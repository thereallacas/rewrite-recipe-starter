/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thereallacas;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AssertEqualsToAssertThatTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AssertEqualsToAssertThat())
          .parser(JavaParser.fromJavaVersion()
            .classpath("junit-jupiter-api"));
    }

    @DocumentExample
    @Test
    void twoArgument() {
        rewriteRun(
          //language=java
          java(
            """
              import org.junit.jupiter.api.Assertions;
              
              class A {
                  void foo() {
                      Assertions.assertEquals(1, 2);
                  }
              }
              """,
            """
              import org.assertj.core.api.Assertions;
              
              class A {
                  void foo() {
                      Assertions.assertThat(2).isEqualTo(1);
                  }
              }
              """
          )
        );
    }

    @Test
    void withDescription() {
        rewriteRun(
          //language=java
          java(
            """
              import org.junit.jupiter.api.Assertions;
              
              class A {
                  void foo() {
                      Assertions.assertEquals(1, 2, "one equals two, everyone knows that");
                  }
              }
              """,
            """
              import org.assertj.core.api.Assertions;
              
              class A {
                  void foo() {
                      Assertions.assertThat(2).as("one equals two, everyone knows that").isEqualTo(1);
                  }
              }
              """
          )
        );
    }
}
