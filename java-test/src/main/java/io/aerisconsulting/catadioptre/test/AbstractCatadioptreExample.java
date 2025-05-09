/*
 * Copyright 2021 AERIS-Consulting e.U.
 *
 * AERIS-Consulting e.U. licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.aerisconsulting.catadioptre.test;

import io.aerisconsulting.catadioptre.KTestable;
import io.aerisconsulting.catadioptre.Testable;

import java.util.Arrays;
import java.util.Objects;

abstract class AbstractCatadioptreExample<T extends Number, V, SELF extends AbstractCatadioptreExample<T, V, SELF>> implements CatadioptreInterface<T> {

    @Testable
    private T typedProperty = null;

    @Testable
    private V typedProperty2 = null;

    protected AbstractCatadioptreExample(final T typedProperty, final V defaultArgumentTypedProperty2) {
        this.typedProperty = typedProperty;
        this.typedProperty2 = defaultArgumentTypedProperty2;
    }

    @KTestable
    private SELF self() {
        return (SELF) this;
    }

    @Testable
    private Double divideSum(double divider, Double... valuesToSum) {
        return Arrays.stream(valuesToSum).filter(Objects::nonNull).mapToDouble(d -> d).sum() / divider;
    }

    @Testable
    private int getAnything() {
        return 123;
    }
}
