package com.campushub.stack;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.FederatedPrincipal;
import software.amazon.awscdk.services.iam.OpenIdConnectProvider;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class GithubActionsOidcRoleStack extends Stack {

    public GithubActionsOidcRoleStack(
            final Construct scope,
            final String id,
            final StackProps props,
            final String githubOwner,
            final String githubRepo,
            final String githubBranch
    ) {
        super(scope, id, props);

        OpenIdConnectProvider provider = OpenIdConnectProvider.Builder.create(this, "GithubOidcProvider")
                .url("https://token.actions.githubusercontent.com")
                .clientIds(List.of("sts.amazonaws.com"))
                .build();

        String repositoryRef = String.format("repo:%s/%s:ref:refs/heads/%s", githubOwner, githubRepo, githubBranch);

        Role role = Role.Builder.create(this, "GithubActionsDeployRole")
                .roleName("campus-service-hub-github-actions-role")
                .description("OIDC role assumed by GitHub Actions CD workflow.")
                .assumedBy(new FederatedPrincipal(provider.getOpenIdConnectProviderArn(), Map.of(
                        "StringEquals", Map.of(
                                "token.actions.githubusercontent.com:aud", "sts.amazonaws.com"
                        ),
                        "StringLike", Map.of(
                                "token.actions.githubusercontent.com:sub", repositoryRef
                        )
                ), "sts:AssumeRoleWithWebIdentity"))
                .inlinePolicies(Map.of(
                        "GithubActionsDeployPolicy",
                        PolicyDocument.Builder.create()
                                .statements(List.of(
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of("ecr:GetAuthorizationToken"))
                                                .resources(List.of("*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of(
                                                        "ecr:BatchCheckLayerAvailability",
                                                        "ecr:BatchGetImage",
                                                        "ecr:CompleteLayerUpload",
                                                        "ecr:DescribeImages",
                                                        "ecr:DescribeRepositories",
                                                        "ecr:GetDownloadUrlForLayer",
                                                        "ecr:InitiateLayerUpload",
                                                        "ecr:ListImages",
                                                        "ecr:PutImage",
                                                        "ecr:UploadLayerPart"
                                                ))
                                                .resources(List.of("*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of(
                                                        "ecs:DescribeClusters",
                                                        "ecs:DescribeServices",
                                                        "ecs:ListServices",
                                                        "ecs:UpdateService"
                                                ))
                                                .resources(List.of("*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of(
                                                        "cloudformation:DescribeStackResources",
                                                        "cloudformation:DescribeStacks"
                                                ))
                                                .resources(List.of("*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of("ssm:GetParameter"))
                                                .resources(List.of("arn:aws:ssm:*:*:parameter/cdk-bootstrap/*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of("sts:AssumeRole"))
                                                .resources(List.of("arn:aws:iam::*:role/cdk-hnb659fds-*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of(
                                                        "s3:GetBucketLocation",
                                                        "s3:ListBucket"
                                                ))
                                                .resources(List.of("arn:aws:s3:::cdk-hnb659fds-assets-*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .effect(Effect.ALLOW)
                                                .actions(List.of(
                                                        "s3:GetObject",
                                                        "s3:PutObject",
                                                        "s3:DeleteObject"
                                                ))
                                                .resources(List.of("arn:aws:s3:::cdk-hnb659fds-assets-*/*"))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        CfnOutput.Builder.create(this, "GithubActionsRoleArn")
                .value(role.getRoleArn())
                .build();
    }
}
