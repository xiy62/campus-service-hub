package com.campushub.stack;


import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalStack extends Stack {
    private final Vpc vpc;
    private final Cluster ecsCluster;
    private final int desiredServiceCount;

    public LocalStack(final App scope, final String id, final StackProps props) {

        super(scope, id, props);

        this.vpc = createVpc();
        this.desiredServiceCount = defaultDesiredServiceCount();

        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", dbNameForService("auth-service"));
        DatabaseInstance requestServiceDb = createDatabase("RequestServiceDB", dbNameForService("request-service"));
        this.ecsCluster = createEcsCluster();

        FargateService authService =
                createFargateService("AuthService",
                        "auth-service",
                        List.of(4005),
                        authServiceDb,
                        Map.of("JWT_SECRET", "Q2hvbGNvbGF0ZXMgdGV4dCB3aXRoIDMyYnl0ZXMgaGVyZSE"));
        authService.getNode().addDependency(authServiceDb);

        FargateService billingService =
                createFargateService("BillingService",
                        "billing-service",
                        List.of(4001,9001),
                        null,
                        null);

        FargateService analyticsService =
                createFargateService("AnalyticsService",
                        "analytics-service",
                        List.of(4002),
                        null,
                        null);

        FargateService requestService = createFargateService("RequestService",
                "request-service",
                List.of(4000),
                requestServiceDb,
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "billing-service.campus-service-hub.local",
                        "BILLING_SERVICE_GRPC_PORT", "9001"
                ));
        requestService.getNode().addDependency(requestServiceDb);
        requestService.getNode().addDependency(billingService);

        createApiGatewayService();
    }

    private Vpc createVpc() {
        return Vpc.Builder
                .create(this, "CampusServiceHubVPC")
                .vpcName("CampusServiceHubVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabase(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()))
                .vpc(this.vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("root"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "CampusServiceHubCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        // auth-service.campus-service-hub.local
                        .name("campus-service-hub.local")
                        .build())
                .build();
    }

    private FargateService createFargateService(String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> additionalEnvVars) {
        FargateTaskDefinition taskDefinition =
                FargateTaskDefinition.Builder.create(this, id + "Task")
                        .cpu(256)
                        .memoryLimitMiB(512)
                        .build();

        ContainerDefinitionOptions.Builder containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(containerImageForService(imageName))
                        .portMappings(ports.stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                        .logGroupName("/ecs/" + imageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix(imageName)
                                .build()));


        Map<String, String> envVars = new HashMap<>();
        envVars.put(
                "SPRING_KAFKA_BOOTSTRAP_SERVERS",
                System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka-bootstrap-not-configured:9092")
        );

        if (additionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        if (db != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    dbNameForService(imageName)
            ));
            envVars.put("SPRING_DATASOURCE_USERNAME", "root");
            envVars.put("SPRING_DATASOURCE_PASSWORD",
                    db.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        containerOptions.environment(envVars);
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .desiredCount(this.desiredServiceCount)
                .build();
    }

    private void createApiGatewayService() {
        FargateTaskDefinition taskDefinition =
                FargateTaskDefinition.Builder.create(this, "APIGatewayTaskDefinition")
                        .cpu(256)
                        .memoryLimitMiB(512)
                        .build();

        ContainerDefinitionOptions containerOptions =
                ContainerDefinitionOptions.builder()
                        .image(containerImageForService("api-gateway"))
                        .environment(Map.of(
                                "SPRING_PROFILES_ACTIVE", "prod",
                                "AUTH_SERVICE_URL", "http://auth-service.campus-service-hub.local:4005",
                                "REQUEST_SERVICE_URL", "http://request-service.campus-service-hub.local:4000"
                        ))
                        .portMappings(List.of(4004).stream()
                                .map(port -> PortMapping.builder()
                                        .containerPort(port)
                                        .hostPort(port)
                                        .protocol(Protocol.TCP)
                                        .build())
                                .toList())
                        .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                        .logGroupName("/ecs/api-gateway")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix("api-gateway")
                                .build()))
                        .build();

        taskDefinition.addContainer("APIGatewayContainer", containerOptions);

        ApplicationLoadBalancedFargateService apiGateway =
                ApplicationLoadBalancedFargateService.Builder.create(this, "APIGatewayService")
                        .cluster(ecsCluster)
                        .serviceName("api-gateway")
                        .taskDefinition(taskDefinition)
                        .desiredCount(this.desiredServiceCount)
                        .healthCheckGracePeriod(Duration.seconds(60))
                        .build();
    }

    private ContainerImage containerImageForService(String serviceName) {
        String repositoryName = repositoryNameForService(serviceName);
        IRepository repository = Repository.fromRepositoryName(
                this,
                repositoryImportIdForService(serviceName),
                repositoryName
        );
        return ContainerImage.fromEcrRepository(repository, "latest");
    }

    private String repositoryNameForService(String serviceName) {
        return "campus/" + serviceName;
    }

    private String repositoryImportIdForService(String serviceName) {
        return serviceName.replaceAll("[^A-Za-z0-9]", "") + "Repository";
    }

    private int defaultDesiredServiceCount() {
        String desiredCount = System.getenv().getOrDefault("ECS_SERVICE_DESIRED_COUNT", "1");
        try {
            int parsed = Integer.parseInt(desiredCount);
            return Math.max(parsed, 1);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String dbNameForService(String serviceName) {
        // RDS DBName must be alphanumeric and start with a letter.
        return serviceName.replaceAll("[^A-Za-z0-9]", "") + "db";
    }

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir(cdkOutDir()).build());

        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        new LocalStack(app, "campus-service-hub", props);
        app.synth();
        System.out.println("App synthesizing in process...");
    }

    private static String cdkOutDir() {
        return System.getenv().getOrDefault("CDK_OUTDIR", "./cdk.out");
    }
}
