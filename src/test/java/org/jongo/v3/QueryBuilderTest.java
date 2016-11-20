package org.jongo.v3;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jongo.util.JongoTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class QueryBuilderTest extends JongoTestCase {

    private MongoCollection<Bson> collection;

    @Before
    public void setUp() throws Exception {
        collection = createNewCollection("friends").withWriteConcern(WriteConcern.ACKNOWLEDGED);
    }

    @Test
    public void canQueryWithABuilder() throws Exception {

        Document document = new Document("name", "Abby").append("address", "123 Wall Street");

        collection.insertOne(document);

        Bson result = collection.find(new Document("address", "123 Wall Street")).first();
        BsonDocument bsonDocument = result.toBsonDocument(DBObject.class, MongoClient.getDefaultCodecRegistry());
        assertThat(bsonDocument.toJson()).contains("123 Wall Street");

    }
}
