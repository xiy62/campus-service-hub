package com.campushub.stack;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.LifecycleRule;
import software.amazon.awscdk.services.ecr.Repository;
import software.constructs.Construct;

import java.util.List;

public class EcrRepositoriesStack extends Stack {

    public EcrRepositoriesStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Repository authRepo = createRepository("AuthServiceRepo", "campus/auth-service");
        Repository requestRepo = createRepository("RequestServiceRepo", "campus/request-service");
        Repository billingRepo = createRepository("BillingServiceRepo", "campus/billing-service");
        Repository analyticsRepo = createRepository("AnalyticsServiceRepo", "campus/analytics-service");
        Repository apiGatewayRepo = createRepository("ApiGatewayRepo", "campus/api-gateway");

        output("EcrRepositoryAuthServiceName", authRepo.getRepositoryName());
        output("EcrRepositoryRequestServiceName", requestRepo.getRepositoryName());
        output("EcrRepositoryBillingServiceName", billingRepo.getRepositoryName());
        output("EcrRepositoryAnalyticsServiceName", analyticsRepo.getRepositoryName());
        output("EcrRepositoryApiGatewayName", apiGatewayRepo.getRepositoryName());
    }

    private Repository createRepository(String id, String repositoryName) {
        return Repository.Builder.create(this, id)
                .repositoryName(repositoryName)
                .imageScanOnPush(true)
                .removalPolicy(RemovalPolicy.RETAIN)
                .lifecycleRules(List.of(
                        LifecycleRule.builder()
                                .maxImageCount(30)
                                .build()
                ))
                .build();
    }

    private void output(String outputId, String value) {
        CfnOutput.Builder.create(this, outputId)
                .value(value)
                .build();
    }
}
