/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2015-11-16 19:10:01 UTC)
 * on 2016-01-03 at 00:51:16 UTC 
 * Modify at your own risk.
 */

package com.appspot.skoozi_959.skooziqna.model;

/**
 * Collection of AnswerMessages
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the skooziqna. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class CoreModelsAnswerMessageCollection extends com.google.api.client.json.GenericJson {

  /**
   * Post that stores an answer
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<CoreModelsAnswerMessage> answers;

  static {
    // hack to force ProGuard to consider CoreModelsAnswerMessage used, since otherwise it would be stripped out
    // see https://github.com/google/google-api-java-client/issues/543
    com.google.api.client.util.Data.nullOf(CoreModelsAnswerMessage.class);
  }

  /**
   * Post that stores an answer
   * @return value or {@code null} for none
   */
  public java.util.List<CoreModelsAnswerMessage> getAnswers() {
    return answers;
  }

  /**
   * Post that stores an answer
   * @param answers answers or {@code null} for none
   */
  public CoreModelsAnswerMessageCollection setAnswers(java.util.List<CoreModelsAnswerMessage> answers) {
    this.answers = answers;
    return this;
  }

  @Override
  public CoreModelsAnswerMessageCollection set(String fieldName, Object value) {
    return (CoreModelsAnswerMessageCollection) super.set(fieldName, value);
  }

  @Override
  public CoreModelsAnswerMessageCollection clone() {
    return (CoreModelsAnswerMessageCollection) super.clone();
  }

}
