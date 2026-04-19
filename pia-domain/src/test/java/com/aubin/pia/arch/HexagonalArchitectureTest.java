package com.aubin.pia.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.aubin.pia.domain",
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring =
            noClasses()
                    .that()
                    .resideInAPackage("com.aubin.pia.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..")
                    .because("the domain must remain framework-free");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_aws =
            noClasses()
                    .that()
                    .resideInAPackage("com.aubin.pia.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("software.amazon..", "com.amazonaws..")
                    .because("the domain must not know about AWS");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_jackson =
            noClasses()
                    .that()
                    .resideInAPackage("com.aubin.pia.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("com.fasterxml.jackson..")
                    .because("the domain must not know about serialisation frameworks");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_persistence =
            noClasses()
                    .that()
                    .resideInAPackage("com.aubin.pia.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
                    .because("the domain must remain persistence-ignorant");
}
