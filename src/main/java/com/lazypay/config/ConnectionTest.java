package com.lazypay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Component
public class ConnectionTest implements CommandLineRunner {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔍 Testing MongoDB connection...");
        System.out.println("📡 URI: " + mongoUri);
        System.out.println("📊 Database: " + databaseName);
        
        try {
            // Create MongoDB client
            MongoClient mongoClient = MongoClients.create(mongoUri);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            
            System.out.println("✅ MongoDB client created successfully");
            System.out.println("✅ Database reference obtained: " + database.getName());
            
            // Test connection by listing collections
            Publisher<String> collections = database.listCollectionNames();
            
            collections.subscribe(new Subscriber<String>() {
                @Override
                public void onSubscribe(Subscription s) {
                    System.out.println("📋 Listing collections...");
                    s.request(10); // Request up to 10 collection names
                }
                
                @Override
                public void onNext(String collectionName) {
                    System.out.println("   📁 Collection: " + collectionName);
                }
                
                @Override
                public void onError(Throwable t) {
                    System.err.println("❌ Error listing collections: " + t.getMessage());
                }
                
                @Override
                public void onComplete() {
                    System.out.println("✅ Collection listing completed");
                    System.out.println("🎯 MongoDB connection test successful!");
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Failed to create MongoDB client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
