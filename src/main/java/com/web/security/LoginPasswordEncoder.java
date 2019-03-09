package com.web.security;


import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author z
 */
public class LoginPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (null == rawPassword || null == encodedPassword) {
            return false;
        }
        return rawPassword.toString().equals(encodedPassword);
    }

//    private static byte[] hash(String userName, byte[] passwd) throws GeneralSecurityException {
//        MessageDigest md5 = MessageDigest.getInstance("MD5");
//        md5.update(StringUtils.toBytesQuietly(userName));
//        md5.update(passwd);
//        return md5.digest();
//    }

}
