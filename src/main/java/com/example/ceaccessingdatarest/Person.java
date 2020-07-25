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
/* - JPA/JSON tools needs a no-args constructor.
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