package com.FourWings.atcSystem;

import com.FourWings.atcSystem.frontend.RegistrationPageController;
import com.FourWings.atcSystem.model.user.UserService;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistrationPageControllerTest {

    private RegistrationPageController controller;
    private UserService userService;
    private Label mockLabel;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        controller = new RegistrationPageController(userService);
    }

    @Test
    void validateForm_EmptyUsernameAndPassword_Returns1() {
        assertEquals(1, controller.validateForm("", ""));
    }

    @Test
    void validateForm_EmptyUsername_Returns1() {
        assertEquals(1, controller.validateForm("", "password123"));
    }

    @Test
    void validateForm_EmptyPassword_Returns1() {
        assertEquals(1, controller.validateForm("Erik123", ""));
    }


    @Test
    void validateForm_InvalidUsernameSymbols_Returns2() {
        assertEquals(2, controller.validateForm("!!!", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameTooShort_Returns2() {
        assertEquals(2, controller.validateForm("ab", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameTooLong_Returns2() {
        assertEquals(2, controller.validateForm("abcdefghijklmnopqrstuv", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameWithSpace_Returns2() {
        assertEquals(2, controller.validateForm("Erik 123", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameWithAccent_Returns2() {
        assertEquals(2, controller.validateForm("Érik", "password123"));
    }

    @Test
    void validateForm_PasswordTooShort_Returns3() {
        assertEquals(3, controller.validateForm("Erik123", "123"));
    }

    @Test
    void validateForm_PasswordExactly7Chars_Returns3() {
        assertEquals(3, controller.validateForm("Erik123", "1234567"));
    }

    @Test
    void validateForm_PasswordEmptyAndInvalidUsername_Returns1() {
        assertEquals(1, controller.validateForm("", ""));
    }

    @Test
    void validateForm_PasswordShortAndInvalidUsername_Returns2() {
        assertEquals(2, controller.validateForm("!!", "123"));
    }

    @Test
    void validateForm_ValidSimple_Returns0() {
        assertEquals(0, controller.validateForm("Erik123", "password123"));
    }

    @Test
    void validateForm_ValidWithDot_Returns0() {
        assertEquals(0, controller.validateForm("Erik.123", "password123"));
    }

    @Test
    void validateForm_ValidWithUnderscore_Returns0() {
        assertEquals(0, controller.validateForm("Erik_123", "password123"));
    }

    @Test
    void validateForm_ValidWithDash_Returns0() {
        assertEquals(0, controller.validateForm("Erik-123", "password123"));
    }

    @Test
    void validateForm_ValidMaxLengthUsername_Returns0() {
        assertEquals(0, controller.validateForm("abcdefghijklmnopqrst", "password123"));
    }

    @Test
    void validateForm_ValidPasswordExactly8Chars_Returns0() {
        assertEquals(0, controller.validateForm("Erik123", "12345678"));
    }

    @Test
    void validateForm_UsernameWithNumbersOnly_Returns0() {
        assertEquals(0, controller.validateForm("123456", "password123"));
    }

    @Test
    void validateForm_UsernameWithDotsAndUnderscores_Returns0() {
        assertEquals(0, controller.validateForm("user.name_01", "password123"));
    }

    @Test
    void validateForm_UsernameWithDashAndNumbers_Returns0() {
        assertEquals(0, controller.validateForm("user-99", "securePass1"));
    }

    @Test
    void validateForm_PasswordExactly8Chars_Returns0() {
        assertEquals(0, controller.validateForm("ValidUser", "abcd1234"));
    }

    @Test
    void validateForm_UsernameStartsWithDot_Returns0() {
        assertEquals(0, controller.validateForm(".username", "password123"));
    }

    @Test
    void validateForm_UsernameEndsWithDot_Returns0() {
        assertEquals(0, controller.validateForm("username.", "password123"));
    }

    @Test
    void validateForm_ValidLettersOnly_Returns0() {
        assertEquals(0, controller.validateForm("AbcDef", "password123"));
    }

    @Test
    void validateForm_ValidNumbersOnly_Returns0() {
        assertEquals(0, controller.validateForm("123456", "password123"));
    }

    @Test
    void validateForm_ValidDotUnderscoreDash_Returns0() {
        assertEquals(0, controller.validateForm("a._-z9", "password123"));
    }

    @Test
    void validateForm_ValidMaxLength_Returns0() {
        assertEquals(0, controller.validateForm("abcdefghijklmnopqrst", "password123"));
    }

    @Test
    void validateForm_ValidMinLength_Returns0() {
        assertEquals(0, controller.validateForm("abc", "password123"));
    }

    @Test
    void validateForm_ValidLettersAndNumbers_Returns0() {
        assertEquals(0, controller.validateForm("User123", "password123"));
    }

    @Test
    void validateForm_ValidMixedSpecialChars_Returns0() {
        assertEquals(0, controller.validateForm("A._-9Z", "password123"));
    }

    @Test
    void validateForm_ValidOnlyDots_Returns0() {
        assertEquals(0, controller.validateForm("a.b.c", "password123"));
    }

    @Test
    void validateForm_ValidOnlyUnderscores_Returns0() {
        assertEquals(0, controller.validateForm("a_b_c", "password123"));
    }

    @Test
    void validateForm_ValidOnlyDashes_Returns0() {
        assertEquals(0, controller.validateForm("a-b-c", "password123"));
    }

    @Test
    void validateForm_ValidPasswordExactly8_Returns0() {
        assertEquals(0, controller.validateForm("ValidUser", "12345678"));
    }

    @Test
    void validateForm_ValidPasswordLonger_Returns0() {
        assertEquals(0, controller.validateForm("ValidUser", "longpassword123"));
    }

    @Test
    void validateForm_InvalidTooShortUsername_Returns2() {
        assertEquals(2, controller.validateForm("ab", "password123"));
    }

    @Test
    void validateForm_InvalidTooLongUsername_Returns2() {
        assertEquals(2, controller.validateForm("abcdefghijklmnopqrstu", "password123"));
    }


    @Test
    void validateForm_InvalidUsernameWithAtSymbol_Returns2() {
        assertEquals(2, controller.validateForm("user@name", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameWithExclamation_Returns2() {
        assertEquals(2, controller.validateForm("user!", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameWithAccentedLetter_Returns2() {
        assertEquals(2, controller.validateForm("Érik", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameStartsWithDot_Returns0() {
        assertEquals(0, controller.validateForm(".ab", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameStartsWithUnderscore_Returns0() {
        assertEquals(0, controller.validateForm("_ab", "password123"));
    }

    @Test
    void validateForm_InvalidUsernameStartsWithDash_Returns0() {
        assertEquals(0, controller.validateForm("-ab", "password123"));
    }

    @Test
    void validateForm_PasswordExactly7_Returns3() {
        assertEquals(3, controller.validateForm("ValidUser", "1234567"));
    }


}

