/**
 * Copyright (C) 2012 - 2014 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.cryptsy.service;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.RestInvocation;

/**
 * This may be used as the value of a @HeaderParam, @QueryParam or @PathParam to create a digest of the post body (composed of @FormParam's). Don't use as the value of a @FormParam, it will probably
 * cause an infinite loop.
 * <p>
 * This may be used for REST APIs where some parameters' values must be digests of other parameters. An example is the MtGox API v1, where the Rest-Sign header parameter must be a digest of the
 * request body (which is composed of @FormParams).
 * </p>
 */
public class CryptsyHmacPostBodyDigest implements ParamsDigest {
  
  private static final String HMAC_SHA_512 = "HmacSHA512";
  private final Mac           mac;
  
  /**
   * Constructor
   * 
   * @param secretKeyBase64
   * @throws IllegalArgumentException if key is invalid (cannot be base-64-decoded or the decoded key is invalid).
   */
  private CryptsyHmacPostBodyDigest(String secretKeyBase64) throws IllegalArgumentException {
  
    try {
      SecretKey secretKey = new SecretKeySpec(secretKeyBase64.getBytes(), HMAC_SHA_512);
      mac = Mac.getInstance(HMAC_SHA_512);
      mac.init(secretKey);
    } catch (InvalidKeyException e) {
      throw new IllegalArgumentException("Invalid key for hmac initialization.", e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Illegal algorithm for post body digest. Check the implementation.");
    }
  }
  
  public static CryptsyHmacPostBodyDigest createInstance(String secretKeyBase64) throws IllegalArgumentException {
  
    return secretKeyBase64 == null ? null : new CryptsyHmacPostBodyDigest(secretKeyBase64);
  }
  
  @Override
  public String digestParams(RestInvocation RestInvocation) {
  
    String postBody = RestInvocation.getRequestBody();
    
    String hmac = "";
    byte[] digest = mac.doFinal(postBody.getBytes());
    BigInteger hash = new BigInteger(1, digest);
    hmac = hash.toString(16);
    
    if (hmac.length() % 2 != 0) {
      hmac = "0" + hmac;
    }
    
    return hmac;
  }
}