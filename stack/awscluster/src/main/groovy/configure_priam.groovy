/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */


//
// configure_priam.groovy
//
// Put our property overrides into Priam's SimpleDB table
//
import com.amazonaws.auth.*
import com.amazonaws.services.simpledb.*
import com.amazonaws.services.simpledb.model.*

String domain = "PriamProperties"

String accessKey = (String)System.getenv().get("AWS_ACCESS_KEY")
String secretKey = (String)System.getenv().get("AWS_SECRET_KEY")
String stackName = (String)System.getenv().get("STACK_NAME")
String hostName  = (String)System.getenv().get("PUBLIC_HOSTNAME")

def creds = new BasicAWSCredentials(accessKey, secretKey)
def sdbClient = new AmazonSimpleDBClient(creds)

// creates domain or no-op if it already exists
sdbClient.createDomain(new CreateDomainRequest(domain))

def props = new Properties()
props.load(new FileInputStream("../conf/Priam.properties"))

for (name in props.stringPropertyNames()) {

    def value = props.getProperty(name)
    def key = "${stackName}${name}"

    def attrs = new ArrayList()
    attrs.add(new ReplaceableAttribute("appId", stackName, true))
    attrs.add(new ReplaceableAttribute("property", name, true))
    attrs.add(new ReplaceableAttribute("value", value, true))

    // this will set new or update existing attributes
    def par = new PutAttributesRequest(domain, key, attrs)
    sdbClient.putAttributes(par);
}

println "Configured Priam."