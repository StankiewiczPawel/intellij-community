/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.theoryinpractice.testng.configuration.testDiscovery;

import com.intellij.execution.CantRunException;
import com.intellij.execution.JavaTestConfigurationBase;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testDiscovery.TestDiscoveryConfigurationProducer;
import com.intellij.execution.testframework.TestSearchScope;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.theoryinpractice.testng.configuration.SearchingForTestsTask;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;
import com.theoryinpractice.testng.configuration.TestNGConfigurationType;
import com.theoryinpractice.testng.configuration.TestNGRunnableState;
import com.theoryinpractice.testng.model.TestData;
import com.theoryinpractice.testng.model.TestNGTestPattern;
import com.theoryinpractice.testng.model.TestType;
import com.theoryinpractice.testng.util.TestNGUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestNGTestDiscoveryConfigurationProducer extends TestDiscoveryConfigurationProducer {
  protected TestNGTestDiscoveryConfigurationProducer() {
    super(TestNGConfigurationType.getInstance());
  }

  @Override
  protected void setPosition(JavaTestConfigurationBase configuration, PsiLocation<PsiMethod> position) {
    ((TestNGConfiguration)configuration).beFromSourcePosition(position);
  }

  @Override
  protected Pair<String, String> getPosition(JavaTestConfigurationBase configuration) {
    final TestData data = ((TestNGConfiguration)configuration).getPersistantData();
    if (data.TEST_OBJECT.equals(TestType.SOURCE.getType())) {
      return Pair.create(data.getMainClassName(), data.getMethodName());
    }
    return null;
  }

  @Override
  public boolean isApplicable(PsiMethod[] methods) {
    return Arrays.stream(methods).anyMatch(method -> TestNGUtil.hasTest(method));
  }

  @Override
  public RunProfileState createProfile(PsiMethod[] testMethods,
                                       Module module,
                                       RunConfiguration configuration,
                                       ExecutionEnvironment environment) {
    return new TestNGRunnableState(environment, (TestNGConfiguration)configuration) {
      @Override
      public SearchingForTestsTask createSearchingForTestsTask() {
        return new SearchingForTestsTask(myServerSocket, getConfiguration(), myTempFile) {
          @Override
          protected void search() throws CantRunException {
            myClasses.clear();
            final Set<String> patterns = new HashSet<>();
            for (PsiMethod method : testMethods) {
              patterns.add(method.getContainingClass().getQualifiedName() + "," + method.getName());
            }
            final Module module = getConfiguration().getConfigurationModule().getModule();
            final GlobalSearchScope searchScope =
              module != null ? GlobalSearchScope.moduleWithDependenciesScope(module) : GlobalSearchScope.projectScope(getProject());
            TestNGTestPattern.fillTestObjects(myClasses, patterns, TestSearchScope.MODULE_WITH_DEPENDENCIES,
                                              getConfiguration(), searchScope);
          }

          @Override
          protected void onFound() {
            super.onFound();
            writeClassesPerModule(myClasses);
          }
        };
      }
    };
  }
}
