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
rootProject.name = "catadioptre"

include("catadioptre-java")
include("catadioptre-kotlin")
include("catadioptre-annotations")

include("examples:catadioptre-kotlin-gradle-kotlin-dsl-example")
include("examples:catadioptre-kotlin-gradle-groovy-dsl-example")

include("examples:catadioptre-java-gradle-kotlin-dsl-example")
include("examples:catadioptre-java-gradle-groovy-dsl-example")
