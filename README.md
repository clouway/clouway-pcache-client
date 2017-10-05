### clouway-pcache-client
Acaching client library that works on GAE

### Adding as dependency

In Gradle:
```groovy

repositories {
  mavenCentral()  
}

dependencies {
     compile 'com.clouway.pcache:clouway-pcache-clouway-gae:0.0.1'
}
```

In Maven:

```xml

 <dependency>
    <groupId>com.clouway.pcache</groupId>
    <artifactId>clouway-pcache-client-gae</artifactId>
    <version>0.0.1</version>
 </dependency>

```

### Adapters
 * Google App Engine

```java
GAECacheManager cacheManager = GaeCacheManagerFactory.create();
```

### Contributing
If you would like to contribute code to pcache client you can do so through GitHub by forking the repository and sending
a pull request. When submitting code, please make every effort to follow existing conventions and style in order to
keep the code as readable as possible. Please also make sure your code compiles by running gradle clean build.
