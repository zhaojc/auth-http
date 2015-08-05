package org.rootservices.otter.security;

import org.rootservices.authorization.security.*;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by tommackenzie on 8/5/15.
 */
public class RandomStringImpl implements org.rootservices.authorization.security.RandomString {

    private SecureRandom secureRandom = new SecureRandom();

    public RandomStringImpl() {}

    public RandomStringImpl(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    @Override
    public String run() {
        return new BigInteger(130, secureRandom).toString(32);
    }
}