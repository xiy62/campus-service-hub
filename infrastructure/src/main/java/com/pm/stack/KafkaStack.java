package com.pm.stack;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.constructs.Construct;

import java.util.List;
import java.util.stream.Collectors;

public class KafkaStack extends Stack {

    public KafkaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        IVpc vpc = Vpc.fromLookup(this, "CampusServiceHubVpcLookup", VpcLookupOptions.builder()
                .vpcName("CampusServiceHubVPC")
                .build());

        List<String> privateSubnets = vpc.selectSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .getSubnets()
                .stream()
                .map(subnet -> subnet.getSubnetId())
                .collect(Collectors.toList());

        CfnCluster mskCluster = CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("3.8.x")
                .numberOfBrokerNodes(2)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.t3.small")
                        .clientSubnets(privateSubnets)
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();

        CfnOutput.Builder.create(this, "KafkaClusterArn")
                .value(mskCluster.getRef())
                .build();
    }
}
