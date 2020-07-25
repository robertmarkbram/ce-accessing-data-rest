# Coding Exercise: Accessing JPA Data with REST

Source: [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/). This exercise combines

- [Spring HATEOAS](https://spring.io/projects/spring-hateoas)
  - > Hypermedia as the Engine of Application State (HATEOAS) is a component of the REST application architecture that distinguishes it from other network application architectures.
    > 
    > With HATEOAS, a client interacts with a network application whose application servers provide information dynamically through hypermedia. A REST client needs little to no prior knowledge about how to interact with an application or server beyond a generic understanding of hypermedia.
    > 
    > _-- Source: [HATEOAS](https://en.wikipedia.org/wiki/HATEOAS)_
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

What I will build.

 > You will build a Spring application that lets you create and retrieve Person objects stored in a database by using Spring Data REST. Spring Data REST takes the features of Spring HATEOAS and Spring Data JPA and automatically combines them together.

# Create and run the app

1. [Starting with Spring Initializr](https://spring.io/guides/gs/accessing-data-rest/#scratch)
    1. [Link to starting this project](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.3.1.RELEASE&packaging=jar&jvmVersion=11&groupId=com.example&artifactId=ce-accessing-data-rest&name=ce-accessing-data-rest&description=Coding%20exercise%20to%20show%20how%20to%20access%20data%20via%20RESTful%20interfaces&packageName=com.example.ce-accessing-data-rest&dependencies=data-rest,data-jpa,h2).
    2. Expanded to `C:\Users\Robert Bram\work\personal_projects\Coding-Exercises\ce-accessing-data-rest`
    3. Github
        1. [Create new repository](https://github.com/new)
            1. [robertmarkbram/ce-accessing-data-rest](https://github.com/robertmarkbram/ce-accessing-data-rest)
        2. Upload code.

            ```bash
            cd "~/DirWork/personal_projects/Coding-Exercises/ce-accessing-data-rest"
            echo "# Coding Exercise: Accessing Data via Rest" >> README.md
            git init
            git add .
            git commit -m "ce-accessing-data-rest

            - First cut of code generated from Spring Initializr"
            git remote add origin git@github.com:robertmarkbram/ce-accessing-data-rest.git
            git push -u origin master
            ```

2. Create domain object: `Person.java`

    ```java
    package com.example.ceaccessingdatarest;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.GenerationType;
    import javax.persistence.Id;

    @Builder(toBuilder = true)
    @Data
    // We are saving instances of this object via JPA
    @Entity
    /*- JPA/JSON tools needs a no-args constructor.
      - So does @Data.
      - They instantiate an empty bean and use setters to init data.
    */
    @NoArgsConstructor(force = true)
    // @Builder needs an all-args constructor.
    @AllArgsConstructor
    public class Person {

      @Id
      @GeneratedValue(strategy = GenerationType.AUTO)
      private long id;

      private String firstName;
      private String lastName;

    }
    ```

    **NOTES**.

    1. Do we need a no-arg constructor? **Yes.**

        > The JPA specification requires that all persistent classes have a no-arg constructor. This constructor may be public or protected. Because the compiler automatically creates a default no-arg constructor when no other constructor is defined, only classes that define constructors must also include a no-arg constructor.
        > 
        > -- Source: [Part 2. Java Persistence API - Chapter 4. Entity](https://openjpa.apache.org/builds/1.2.3/apache-openjpa/docs/jpa_overview_pc.html)

    2. Should the class or methods be final? **No.**

        > Entity classes may not be final. No method of an entity class can be final.
        > 
        > -- Source: [Part 2. Java Persistence API - Chapter 4. Entity](https://openjpa.apache.org/builds/1.2.3/apache-openjpa/docs/jpa_overview_pc.html)

    3. Should the fields be final? **No.**

        > JPA does not support static or final fields.
        > 
        > -- Source: [Part 2. Java Persistence API - Chapter 4. Entity](https://openjpa.apache.org/builds/1.2.3/apache-openjpa/docs/jpa_overview_pc.html)

    4. But I can get this code working with a _final class_ **and** _final fields_!

        Different JPA providers support different underlying rules, which sucks.

        >  Yes, there are JPA persistence providers which support final fields, however I recommend not using final fields in your entity classes as the behavior is not defined (and actually differs from provider to provider). Moreover, I think it is a bad idea that a JPA provider changes final fields after construction because visibility guarantees may be violated this way.
        >
        > -- Source: [StackOverflow answer to _Persistence provider for Java that supports final fields_](https://stackoverflow.com/a/26513566/257233)

3. Create a repository for `Person.java`

    Spring repository interfaces give you CRUD actions to perform on specific entities.
    
    We create a [PagingAndSortingRepository](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/PagingAndSortingRepository.html) for this entity here.

    At runtime, [Spring Data REST](https://spring.io/projects/spring-data-rest) will create an implementation of this interface and use the [@RepositoryRestResource](https://docs.spring.io/spring-data/rest/docs/current/api/org/springframework/data/rest/core/annotation/RepositoryRestResource.html) annotation to direct Spring MVC to create RESTful endpoints at `/people`.

    ```java
    package com.example.ceaccessingdatarest;

    import java.util.List;

    import org.springframework.data.repository.PagingAndSortingRepository;
    import org.springframework.data.repository.query.Param;
    import org.springframework.data.rest.core.annotation.RepositoryRestResource;

    @RepositoryRestResource(collectionResourceRel = "people", path = "people")
    public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {

      List<Person> findByLastName(@Param("name") String name);

    }
    ```

    **NOTE**.

      1. `@RepositoryRestResource` is not required for a repository to be exported. It is used only to change the export details, such as using `/people` instead of the default value of `/persons`.
      2. We created a custom query to retrieve a list of `Person` objects based on the `lastName`.

4. Look at the `@SpringBootApplication` class.

    ```java
    package com.example.ceaccessingdatarest;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class CeAccessingDataRestApplication {

      public static void main(String[] args) {
        SpringApplication.run(CeAccessingDataRestApplication.class, args);
      }

    }
    ```

    `@SpringBootApplication` is a convenience annotation that adds all of the following:
    
    1. `@Configuration`: Tags the class as a source of bean definitions for the application context.
    2. `@EnableAutoConfiguration`: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings. For example, if `spring-webmvc` is on the classpath, this annotation flags the application as a web application and activates key behaviors, such as setting up a `DispatcherServlet`.
    3. `@ComponentScan`: Tells Spring to look for other components, configurations, and services in the `com/example` package, letting it find the controllers.
    
    The `main()` method uses Spring Boot’s `SpringApplication.run()` method to launch an application. Did you notice that there was not a single line of XML? There is no `web.xml` file, either. This web application is 100% pure Java and you did not have to deal with configuring any plumbing or infrastructure. 
    
    Spring Boot automatically spins up Spring Data JPA to create a concrete implementation of the `PersonRepository` and configure it to talk to a back end in-memory database by using JPA.  
    
    Spring Data REST builds on top of Spring MVC. It creates a collection of Spring MVC controllers, JSON converters, and other beans to provide a RESTful front end. These components link up to the Spring Data JPA backend. When you use Spring Boot, this is all autoconfigured. If you want to investigate how that works, by looking at the [RepositoryRestMvcConfiguration](https://docs.spring.io/spring-data/rest/docs/current/api/org/springframework/data/rest/webmvc/config/RepositoryRestMvcConfiguration.html) in Spring Data REST.
5. Make sure I am using the same JDK on the command line and within the project.

    The `pom.xml` specifies that I use JDK 14:

    ```xml
    <properties>
      <java.version>14</java.version>
    </properties>
    ```

    So make my `.sourceForIssueWork.sh` file contain a similar declaration:

    ```bash
    JAVA_HOME=/C/Program\ Files/Java/jdk-14.0.1
    PATH="${JAVA_HOME}/bin:${PATH}"
    ```

    And source that when I am running on the command line.

    ```bash
    source ~/ci/bin/.sourceForIssueWork.sh 
    ```

6. Run the application from the project code.

    ```bash
    ./mvnw spring-boot:run
    ```

7. Build an executable JAR and run that.

    ```bash
    ./mvnw clean package
    raven clean package
    java -jar target/ce-accessing-data-rest-0.0.1-SNAPSHOT.jar
    ```

# Test the app

## Discover services

See the top level service, which gives us discoverability.

```bash
$ curl http://localhost:8080
{
  "_links" : {
    "people" : {
      "href" : "http://localhost:8080/people{?page,size,sort}",
      "templated" : true
    },
    "profile" : {
      "href" : "http://localhost:8080/profile"
    }
  }
}
```

There is a `people` link located at http://localhost:8080/people. It has some options, such as `?page`, `?size`, and `?sort`.

**NOTE**. Spring Data REST uses the [HAL format](http://stateless.co/hal_specification.html) for `JSON` output. It is flexible and offers a convenient way to supply links adjacent to the data that is served.

A convenient aspect of this hypermedia-driven interface is that you can discover all the RESTful endpoints by using curl (or whatever REST client you like). You need not exchange a formal contract or interface document with your customers.

## Find all records

See the people records. There are currently no elements and, hence, no pages.

```
$ curl http://localhost:8080/people
{
  "_embedded" : {
    "people" : [ ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/people"
    },
    "search" : {
      "href" : "http://localhost:8080/people/search"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 0,
    "totalPages" : 0,
    "number" : 0
  }
}
```

## Add a record

Add a `Person` record.

```bash
$ curl -i -H "Content-Type:application/json" -d '{"firstName": "Frodo", "lastName": "Baggins"}' http://localhost:8080/people
HTTP/1.1 201
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Location: http://localhost:8080/people/1
Content-Type: application/hal+json
Transfer-Encoding: chunked
Date: Sat, 25 Jul 2020 01:47:05 GMT

{
  "firstName" : "Frodo",
  "lastName" : "Baggins",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/1"
    },
    "person" : {
      "href" : "http://localhost:8080/people/1"
    }
  }
}
```

**Notes**.

- `-i`: Ensures you can see the response message including the headers. The URI of the newly created `Person` is shown.
- `-H "Content-Type:application/json"`: Sets the content type so the application knows the payload contains a JSON object.
- `-d '{"firstName": "Frodo", "lastName": "Baggins"}'`: Is the data being sent.
- If you are on Windows, the command above will work on [WSL - Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl). If you can’t install WSL, you might need to replace the single quotes with double quotes and escape the existing double quotes, i.e. `-d "{\"firstName\": \"Frodo\", \"lastName\": \"Baggins\"}"`.
  - Or use [Cygwin](https://www.cygwin.com/).
- Notice how the response to the `POST` operation includes a `Location` header. This contains the URI of the newly created resource. Spring Data REST also has two methods (`RepositoryRestConfiguration.setReturnBodyOnCreate(…)` and `setReturnBodyOnUpdate(…)`) that you can use to configure the framework to immediately return the representation of the resource just created. `RepositoryRestConfiguration.setReturnBodyForPutAndPost(…)` is a shortcut method to enable representation responses for create and update operations.

Query for all people again.

```bash
$ curl http://localhost:8080/people
{
  "_embedded" : {
    "people" : [ {
      "firstName" : "Frodo",
      "lastName" : "Baggins",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/people/1"
        },
        "person" : {
          "href" : "http://localhost:8080/people/1"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/people"
    },
    "search" : {
      "href" : "http://localhost:8080/people/search"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 1,
    "totalPages" : 1,
    "number" : 0
  }
}
```

The `people` object contains a list that includes `Frodo`. Notice how it includes a `self` link. Spring Data REST also uses [Evo Inflector](https://www.atteo.org/2011/12/12/Evo-Inflector.html) to pluralize the name of the entity for groupings.

## Find individual records

Query individual records using the `self` link to the first person we created.

```bash
$ curl http://localhost:8080/people/1
{
  "firstName" : "Frodo",
  "lastName" : "Baggins",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/1"
    },
    "person" : {
      "href" : "http://localhost:8080/people/1"
    }
  }
}
```

This might appear to be purely web-based. However, behind the scenes, there is an H2 relational database. In production, you would probably use a real one, such as PostgreSQL.

In this guide, there is only one domain object. With a more complex system, where domain objects are related to each other, Spring Data REST renders additional links to help navigate to connected records.

## Find custom queries

Find all the custom queries:

```bash
$ curl http://localhost:8080/people/search
{
  "_links" : {
    "findByLastName" : {
      "href" : "http://localhost:8080/people/search/findByLastName{?name}",
      "templated" : true
    },
    "self" : {
      "href" : "http://localhost:8080/people/search"
    }
  }
}
```

You can see the URL for the query, including the HTTP query parameter, `name`. Note that this matches the `@Param("name")` annotation embedded in the interface.

## Use custom query

The following example shows how to use the `findByLastName` query:

```bash
$ curl http://localhost:8080/people/search/findByLastName?name=Baggins
{
  "_embedded" : {
    "people" : [ {
      "firstName" : "Frodo",
      "lastName" : "Baggins",
      "_links" : {
        "self" : {
          "href" : "http://localhost:8080/people/1"
        },
        "person" : {
          "href" : "http://localhost:8080/people/1"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/search/findByLastName?name=Baggins"
    }
  }
}
```

Because you defined it to return `List<Person>` in the code, it returns all of the results. If you had defined it to return only `Person`, it picks one of the `Person` objects to return. Since this can be unpredictable, you probably do not want to do that for queries that can return multiple entries.

## Use PUT, PATCH and DELETE

You can also issue `PUT`, `PATCH`, and `DELETE` REST calls to replace, update, or delete existing records (respectively). 

### PUT replaces an entire record

`PUT` replaces an entire record. Fields not supplied are replaced with `null`. The following example uses a `PUT` call:

```bash
# Change first name of Frodo to Bilbo.
$ curl -X PUT -H "Content-Type:application/json" -d '{"firstName": "Bilbo", "lastName": "Baggins"}' http://localhost:8080/people/1
{
  "firstName" : "Bilbo",
  "lastName" : "Baggins",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/1"
    },
    "person" : {
      "href" : "http://localhost:8080/people/1"
    }
  }
}

# See updated record.
$ curl http://localhost:8080/people/1
{
  "firstName" : "Bilbo",
  "lastName" : "Baggins",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/1"
    },
    "person" : {
      "href" : "http://localhost:8080/people/1"
    }
  }
}
```

### PATCH updates a subset of the record

You can use `PATCH` to update a subset of items.

The following example uses a PATCH call:

```bash
# Just update the first name.
$ curl -X PATCH -H "Content-Type:application/json" -d '{"firstName": "Bilbo Jr."}' http://localhost:8080/people/1
{
  "firstName" : "Bilbo Jr.",
  "lastName" : "Baggins",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/1"
    },
    "person" : {
      "href" : "http://localhost:8080/people/1"
    }
  }
}

# See updated record.
$ curl http://localhost:8080/people/1
{
  "firstName" : "Bilbo Jr.",
  "lastName" : "Baggins",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people/1"
    },
    "person" : {
      "href" : "http://localhost:8080/people/1"
    }
  }
}
```

### DELETE will delete records

Delete Mr Baggins.

```bash
# Delete the record.
$ curl -X DELETE http://localhost:8080/people/1

# See that there are no records left.
$ curl http://localhost:8080/people
{
  "_embedded" : {
    "people" : [ ]
  },
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/people"
    },
    "profile" : {
      "href" : "http://localhost:8080/profile/people"
    },
    "search" : {
      "href" : "http://localhost:8080/people/search"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 0,
    "totalPages" : 0,
    "number" : 0
  }
}
```

