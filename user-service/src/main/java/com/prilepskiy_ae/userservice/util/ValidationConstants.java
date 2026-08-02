package com.prilepskiy_ae.userservice.util;

import java.util.regex.Pattern;

public final class ValidationConstants {

    public static final int MIN_NAME_LEN = 2;
    public static final int MAX_NAME_LEN = 100;

    public static final int MIN_AGE_LEN = 0;
    public static final int MAX_AGE_LEN = 100;


    public static final int FIRST_INDEX = 0;
    public static final int SHIFT_INDEX = 1;

    public static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zА-Яа-яЁё\\s\\-']+$");


    private ValidationConstants() {}
}
