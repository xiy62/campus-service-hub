package com.campushub.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class GithubActionsOidcRoleApp {

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        String account = System.getenv("CDK_DEFAULT_ACCOUNT");
        String region = System.getenv("CDK_DEFAULT_REGION");

        String githubOwner = requiredEnv("GITHUB_OWNER");
        String githubRepo = requiredEnv("GITHUB_REPO");
        String githubBranch = System.getenv().getOrDefault("GITHUB_BRANCH", "main");

        StackProps props = StackProps.builder()
                .env(Environment.builder()
                        .account(account)
                        .region(region)
                        .build())
                .build();

        new GithubActionsOidcRoleStack(
                app,
                "campus-service-hub-github-oidc",
                props,
                githubOwner,
                githubRepo,
                githubBranch
        );
        app.synth();
        System.out.println("GitHub OIDC role stack synthesized.");
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
