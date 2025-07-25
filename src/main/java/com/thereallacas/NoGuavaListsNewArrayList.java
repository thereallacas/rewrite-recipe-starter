/*
 * Copyright 2021 the original author or authors.
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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.TreeVisitingPrinter;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;

@Value
@EqualsAndHashCode(callSuper = false)
public class NoGuavaListsNewArrayList extends Recipe {
    // These matchers use a syntax described on https://docs.openrewrite.org/reference/method-patterns
    private static final MethodMatcher NEW_ARRAY_LIST = new MethodMatcher("com.google.common.collect.Lists newArrayList()");
    private static final MethodMatcher NEW_ARRAY_LIST_ITERABLE = new MethodMatcher("com.google.common.collect.Lists newArrayList(java.lang.Iterable)");
    private static final MethodMatcher NEW_ARRAY_LIST_CAPACITY = new MethodMatcher("com.google.common.collect.Lists newArrayListWithCapacity(int)");

    @Override
    public String getDisplayName() {
        //language=markdown
        return "Use `new ArrayList<>()` instead of Guava";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Prefer the Java standard library over third-party usage of Guava in simple cases like this.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                // Any change to the AST made by the preconditions check will lead to the visitor returned by Recipe
                // .getVisitor() being applied
                // No changes made by the preconditions check will be kept
                Preconditions.or(
                        new UsesMethod<>(NEW_ARRAY_LIST),
                        new UsesMethod<>(NEW_ARRAY_LIST_ITERABLE),
                        new UsesMethod<>(NEW_ARRAY_LIST_CAPACITY)),
                // To avoid stale state persisting between cycles, getVisitor() should always return a new instance of
                // its visitor
                new JavaVisitor<ExecutionContext>() {
                    // Java Templates are used to generate Java code easily.
                    // They use a syntax that expand Java with possible type-safe insertions points.
                    // See https://docs.openrewrite.org/concepts-and-explanations/javatemplate for full documentation
                    private final JavaTemplate newArrayList = JavaTemplate.builder("new ArrayList<>()")
                            .imports("java.util.ArrayList")
                            .build();

                    private final JavaTemplate newArrayListIterable =
                            JavaTemplate.builder("new ArrayList<>(#{any(java.util.Collection)})")
                                    .imports("java.util.ArrayList")
                                    .build();

                    private final JavaTemplate newArrayListCapacity =
                            JavaTemplate.builder("new ArrayList<>(#{any(int)})")
                                    .imports("java.util.ArrayList")
                                    .build();

                    // This method override is only here to show how to print the AST for debugging purposes.
                    // You can remove this method if you don't need it.
                    @Override
                    public J visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
                        // This is a useful debugging tool if you're ever unsure what the visitor is visiting
                        String printed = TreeVisitingPrinter.printTree(cu);
                        System.out.printf(printed);

                        // You must always delegate to the super method to ensure the visitor continues to visit deeper
                        // return cu; // this leads to a recipe that makes no changes at all
                        return super.visitCompilationUnit(cu, ctx);
                    }

                    // Visit any method invocation, and replace matches with the new ArrayList instantiation.
                    @Override
                    public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        if (NEW_ARRAY_LIST.matches( method )) {
                            maybeRemoveImport( "com.google.common.collect.Lists" );
                            maybeAddImport( "java.util.ArrayList" );
                            return newArrayList.apply( getCursor(), method.getCoordinates().replace() );
                        }
                        if (NEW_ARRAY_LIST_ITERABLE.matches( method )) {
                            maybeRemoveImport( "com.google.common.collect.Lists" );
                            maybeAddImport( "java.util.ArrayList" );
                            return newArrayListIterable.apply( getCursor(), method.getCoordinates().replace(),
                                    method.getArguments().get( 0 ) );
                        }
                        if (NEW_ARRAY_LIST_CAPACITY.matches( method )) {
                            maybeRemoveImport( "com.google.common.collect.Lists" );
                            maybeAddImport( "java.util.ArrayList" );
                            return newArrayListCapacity.apply( getCursor(), method.getCoordinates().replace(),
                                    method.getArguments().get( 0 ) );
                        }
                        return super.visitMethodInvocation( method, ctx );
                    }
                }
        );
    }
}
