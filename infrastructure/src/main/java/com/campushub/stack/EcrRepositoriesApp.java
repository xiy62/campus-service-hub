package com.campushub.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class EcrRepositoriesApp {

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir(cdkOutDir()).build());

        String account = System.getenv("CDK_DEFAULT_ACCOUNT");
        String region = System.getenv("CDK_DEFAULT_REGION");

        StackProps props = StackProps.builder()
                .env(Environment.builder()
                        .account(account)
                        .region(region)
                        .build())
                .build();

        new EcrRepositoriesStack(app, "campus-service-hub-ecr", props);
        app.synth();
        System.out.println("ECR stack synthesized.");
    }

    private static String cdkOutDir() {
        return System.getenv().getOrDefault("CDK_OUTDIR", "./cdk.out");
    }
}
