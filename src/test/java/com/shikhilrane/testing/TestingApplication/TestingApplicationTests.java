package com.shikhilrane.testing.TestingApplication;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Slf4j
// @SpringBootTest     // 1. This is coming from Spring Framework and even if we remove this annotation still it will run as a java file and JUnit is independent of Spring
class TestingApplicationTests {

    @BeforeEach     // 5. This method will run before every single test case
    void setUp(){
        log.info("'BeforeEach', Starting the method, setting up config");
    }

    @AfterEach      // 6. This method will run after every single test case
    void tearDown(){
        log.info("'AfterEach', Tearing down the method");
    }

    @BeforeAll     // 7. This method will run before all test case
    static void setUpOnce(){    // we should use static
        log.info("'BeforeAll', Setup once...");
    }

    @AfterAll      // 8. This method will run after all test case
    static void tearDownOnce(){
        log.info("'AfterAll', Tearing down all...");
    }

    @Test           // 2. Use to state that this is a test case
        // @Disabled    // 3. After applying this annotation, and run all the test cases then this test will be ignored for testing
    void testNumberOne() {
        log.info("Test 1 is running");
    }

    @Test
        // @DisplayName("DisplayNameTwo")  // 4. After applying this annotation, name of the test case will be renamed in console
    void testNumberTwo(){
        log.info("Test 2 is running");
    }


    int addTwoNums(int a, int b){
        return a+b;
    }

    // 9 Assertion using JUnit
    @Test
    void callAddTwoNums(){
        int a = 5;
        int b = 6;
        int result = addTwoNums(a,b);

//        Assertions.assertEquals(11, result);    // pass expected output and method calling with passing arguments, this method from JUnit
        // But we can't do operations like on Strings or Arrays or can't even do chaining
        // So for that we will use AssertJ library
    }

    // 10 Assertion using AssertJ (We will use this)
    @Test
    void callAddTwoNumsWithAssertJ(){
        int a = 5;
        int b = 6;
        int result = addTwoNums(a,b);

        assertThat(result)                                          // Pass the method calling with passing arguments
                .isEqualTo(11)                             // Pass the expected value
                .isCloseTo(6, Offset.offset(8));     // Pass the offset like it can be from 3 4 5 6 7 8 9 10 - 11 - 12 13 14 15 16 17 18 19
    }

    // 11 Assertion using AssertJ for String
    @Test
    void checkStringUsingAssertJ(){
        assertThat("Apple")
                .isEqualTo("Apple")
                .startsWith("Ap")
                .endsWith("le")
                .hasSize(5);
    }

    // 12
    double divideTwoNumbers(int a, int b){
        try {
            return a/b;
        } catch (ArithmeticException e) {
            log.error("Arithmetic Exception Occurred : {}", e.getLocalizedMessage());
            throw new ArithmeticException("Tried to divide by zero");
        }
    }

    @Test
    void testDivideNumbers_whenDenominatorIsZero_thenArithmeticException() {
        int a = 5;
        int b = 0;

        assertThatThrownBy(() -> divideTwoNumbers(a, b))     // assertThatThrownBy checks that the given code throws an exception
                .isInstanceOf(ArithmeticException.class)    // checks that the thrown exception is of type ArithmeticException and if not then test fails
                .hasMessage("Tried to divide by zero");     // checks that the exception message is "Tried to divide by zero" and if not then test fails
    }
}
