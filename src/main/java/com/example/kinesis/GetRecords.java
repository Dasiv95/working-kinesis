//snippet-sourcedescription:[GetRecords.java demonstrates how to read multiple data records from an Amazon Kinesis data stream.]
//snippet-keyword:[AWS SDK for Java v2]
//snippet-keyword:[Code Sample]
//snippet-keyword:[Amazon Kinesis]
//snippet-sourcetype:[full-example]
//snippet-sourcedate:[11/04/2020]
//snippet-sourceauthor:[scmacdon AWS]
/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.kinesis;

//snippet-start:[kinesis.java2.getrecord.import]
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import java.util.ArrayList;
import java.util.List;
//snippet-end:[kinesis.java2.getrecord.import]

/**
 * Demonstrates how to read data from a Amazon Kinesis Data Stream. Before running this Java code example, populate a Data Stream
 * by running the StockTradesWriter example. That example populates a Data Stream that you can then use for this example.
 * Also, ensure that you have setup your development environment, including your credentials.
 *
 * For information, see this documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */

public class GetRecords {

    public static void main(String[] args) {

        final String USAGE = "\n" +
                "Usage:\n" +
                "    GetRecords <streamName>\n\n" +
                "Where:\n" +
                "    streamName - The Amazon Kinesis data stream to read from (for example, StockTradeStream).\n\n" +
                "Example:\n" +
                "    GetRecords streamName\n";

//        if (args.length != 1) {
//            System.out.println(USAGE);
//            System.exit(1);
//        }

//        String streamName = args[0];
        Region region = Region.US_EAST_1;
        KinesisClient kinesisClient = KinesisClient.builder()
                .region(region)
                .build();

        System.out.println("this is before getStockRecords");
        getStockTrades(kinesisClient,"loan-origination");
        System.out.println("this is after getStockRecords");
        kinesisClient.close();
    }

    // snippet-start:[kinesis.java2.getrecord.main]
    public static void getStockTrades(KinesisClient kinesisClient, String streamName) {

        System.out.println("this is in getStockRecords");
            String shardIterator;
            String lastShardId = null;

            // Retrieve the Shards from a Stream
            DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder()
                    .streamName(streamName)
                    .build();
            List<Shard> shards = new ArrayList<>();
        System.out.println("this is after descirbe");

            DescribeStreamResponse streamRes;
            do {
                streamRes = kinesisClient.describeStream(describeStreamRequest);
                shards.addAll(streamRes.streamDescription().shards());

                System.out.println(streamRes.toString());
                System.out.println("this is before shard.size");
                System.out.println(shards.size());
                if (shards.size() > 0) {
                    lastShardId = shards.get(shards.size() - 1).shardId();
                }
            } while (streamRes.streamDescription().hasMoreShards());

            GetShardIteratorRequest itReq = GetShardIteratorRequest.builder()
                    .streamName(streamName)
                    .shardIteratorType("AT_SEQUENCE_NUMBER")
                    .startingSequenceNumber("49621228917141079352170312539352286205301331711745851394")
                    .shardId(shards.get(0).shardId())
                    .build();

            GetShardIteratorResponse shardIteratorResult = kinesisClient.getShardIterator(itReq);
            shardIterator = shardIteratorResult.shardIterator();

            // Continuously read data records from shard.
            List<Record> records;

            // Create new GetRecordsRequest with existing shardIterator.
            // Set maximum records to return to 1000.
            GetRecordsRequest recordsRequest = GetRecordsRequest.builder()
                     .shardIterator(shardIterator)
                     .limit(1000)
                     .build();
        System.out.println("this is before getRecordResponse");

           GetRecordsResponse result = kinesisClient.getRecords(recordsRequest);

           // Put result into record list. Result may be empty.
           records = result.records();

        System.out.println("this is after getRecordsResponse");
        System.out.println(records.size());
        System.out.println(records.toString());


            // Print records
            for (Record record : records) {
                System.out.println("this is the record");
                SdkBytes byteBuffer = record.data();
                System.out.println(String.format("Seq No: %s - %s", record.sequenceNumber(),
                 new String(byteBuffer.asByteArray())));
                System.out.println(new String(byteBuffer.asByteArray()));
                String[] facility = new String(byteBuffer.asByteArray()).split("text/plain\"");
                System.out.println(facility[1]);
             }
         }
        // snippet-end:[kinesis.java2.getrecord.main]
  }

