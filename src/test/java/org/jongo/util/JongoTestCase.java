/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jongo.util;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import org.bson.conversions.Bson;
import org.jongo.Jongo;
import org.jongo.JongoNative;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.model.ExternalType;
import org.junit.BeforeClass;

import java.net.UnknownHostException;

import static org.jongo.marshall.jackson.JacksonMapper.Builder.jacksonMapper;
import static org.junit.Assume.assumeTrue;

public abstract class JongoTestCase {

    private static MongoResource mongoResource;

    private Jongo jongo;
    private JongoNative jongoNative;
    private Mapper mapper;

    public JongoTestCase() {
        this(jacksonMapper().registerModule(new SimpleModule() {{
            this.setMixInAnnotation(ExternalType.class, ExternalType.ExternalTypeMixin.class);
        }}).build());
    }

    protected JongoTestCase(Mapper mapper) {
        this.mapper = mapper;
        this.jongo = new Jongo(mongoResource.getDb("test_jongo"), mapper);
        this.jongoNative = Jongo.useNative(mongoResource.getDatabase("test_jongo"), mapper);
    }

    @BeforeClass
    public static void startMongo() throws Exception {
        mongoResource = new MongoResource();
    }

    protected MongoCollection createEmptyCollection(String collectionName) {
        MongoCollection col = jongo.getCollection(collectionName);
        col.drop();
        return col;
    }

    protected <T> com.mongodb.client.MongoCollection<T> createNewCollection(String collectionName, Class<T> clazz) {
        com.mongodb.client.MongoCollection<T> col = jongoNative.getCollection(collectionName, clazz);
        col.drop();
        return col;
    }

    protected com.mongodb.client.MongoCollection<Bson> createNewCollection(String collectionName) {
        com.mongodb.client.MongoCollection<Bson> col = jongoNative.getCollection(collectionName);
        col.drop();
        return col;
    }

    protected void dropCollection(String collectionName) {
        getDatabase().getCollection(collectionName).drop();
    }

    protected DB getDatabase() {
        return jongo.getDatabase();
    }

    protected Jongo getJongo() {
        return jongo;
    }

    protected Mapper getMapper() {
        return mapper;
    }

    protected Bson q(String query, Object... parameters) {
        return jongoNative.query(query, parameters);
    }

    protected Bson id(Object id) {
        return jongoNative.id(id);
    }

    protected void assumeThatMongoVersionIsGreaterThan(String expectedVersion) throws UnknownHostException {
        int expectedVersionAsInt = Integer.valueOf(expectedVersion.replaceAll("\\.", ""));
        CommandResult buildInfo = getDatabase().command("buildInfo");
        String version = (String) buildInfo.get("version");
        int currentVersion = Integer.valueOf(version.replaceAll("\\.", ""));
        assumeTrue(currentVersion >= expectedVersionAsInt);
    }

    public void prepareMarshallingStrategy(Mapper mapper) {
        this.mapper = mapper;
        this.jongo = new Jongo(mongoResource.getDb("test_jongo"), mapper);
    }

}
