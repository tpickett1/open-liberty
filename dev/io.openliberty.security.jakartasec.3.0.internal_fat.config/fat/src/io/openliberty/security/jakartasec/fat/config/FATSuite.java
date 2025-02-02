/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.security.jakartasec.fat.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import componenttest.custom.junit.runner.AlwaysPassesTest;
import io.openliberty.security.jakartasec.fat.config.tests.ConfigurationClaimsDefinitionTests;
import io.openliberty.security.jakartasec.fat.config.tests.ConfigurationELValuesOverrideTests;
import io.openliberty.security.jakartasec.fat.config.tests.ConfigurationELValuesOverrideWithoutHttpSessionTests;
import io.openliberty.security.jakartasec.fat.config.tests.ConfigurationTests;
import io.openliberty.security.jakartasec.fat.config.tests.ConfigurationUserInfoTests;

@RunWith(Suite.class)
@SuiteClasses({
                AlwaysPassesTest.class,
                ConfigurationTests.class,
                ConfigurationClaimsDefinitionTests.class,
                // LogoutDefinition tests are handled in a separate FAT project as the test use sleeps to wait for tokens to expire and that causes the tests to take quite some time to run
                ConfigurationELValuesOverrideTests.class,
                ConfigurationELValuesOverrideWithoutHttpSessionTests.class,
                ConfigurationUserInfoTests.class
})
public class FATSuite {

}
