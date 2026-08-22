package com.nutzycraft.backend.service;

/**
 * @deprecated This service has been replaced by {@link PayHereService} as part of
 * the agency model pivot. All payments now route exclusively to the Nutzycraft Pvt Ltd
 * PayHere merchant account. This file can be safely deleted along with the stripe-java
 * dependency in pom.xml once the PayHere integration is fully live.
 */
@Deprecated
public class StripeService {
    // No-op — replaced by PayHereService
}
