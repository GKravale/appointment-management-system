package com.appointment.system.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    @DisplayName("onCreate() should set createdAt to current time")
    void onCreate_setsCreatedAt() {
        User user = new User();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isAfter(before));
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("onCreate() should not overwrite existing createdAt on repeated calls")
    void onCreate_calledTwice_updatesTimestamp() {
        User user = new User();
        user.onCreate();
        LocalDateTime first = user.getCreatedAt();

        user.onCreate();

        assertNotNull(user.getCreatedAt());

        assertFalse(user.getCreatedAt().isBefore(first));
    }
}

class PersonEntityTest {

    @Test
    @DisplayName("onCreate() should set createdAt to current time")
    void onCreate_setsCreatedAt() {
        Person person = new Person("Anna", "Bērziņa", "+37122233344");
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        person.onCreate();

        assertNotNull(person.getCreatedAt());
        assertTrue(person.getCreatedAt().isAfter(before));
    }

    @Test
    @DisplayName("Constructor should set firstName, lastName, phoneNr")
    void constructor_setsFields() {
        Person person = new Person("Līga", "Ozola", "+37122233237");

        assertEquals("Līga", person.getFirstName());
        assertEquals("Ozola", person.getLastName());
        assertEquals("+37122233237", person.getPhoneNr());
        assertFalse(person.getIsDeleted());
    }

    @Test
    @DisplayName("isDeleted should default to false")
    void isDeleted_defaultsFalse() {
        Person person = new Person();
        assertFalse(person.getIsDeleted());
    }
}

class AppointmentEntityTest {

    @Test
    @DisplayName("onCreate() should set createdAt to current time")
    void onCreate_setsCreatedAt() {
        Appointment appointment = new Appointment();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        appointment.onCreate();

        assertNotNull(appointment.getCreatedAt());
        assertTrue(appointment.getCreatedAt().isAfter(before));
    }

    @Test
    @DisplayName("changes list should be initialised as empty, not null")
    void changes_initialisedAsEmptyList() {
        Appointment appointment = new Appointment();
        assertNotNull(appointment.getChanges());
        assertTrue(appointment.getChanges().isEmpty());
    }

    @Test
    @DisplayName("Default status should be null before explicit set")
    void status_nullByDefault() {
        Appointment appointment = new Appointment();
        assertNull(appointment.getStatus());
    }
}

class AppointmentChangeEntityTest {

    @Test
    @DisplayName("onCreate() should set changedAt to current time")
    void onCreate_setsChangedAt() {
        AppointmentChange change = new AppointmentChange();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        change.onCreate();

        assertNotNull(change.getChangedAt());
        assertTrue(change.getChangedAt().isAfter(before));
    }
}

class NotificationEntityTest {

    @Test
    @DisplayName("onCreate() should set createdAt to current time")
    void onCreate_setsCreatedAt() {
        Notification notification = new Notification();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        notification.onCreate();

        assertNotNull(notification.getCreatedAt());
        assertTrue(notification.getCreatedAt().isAfter(before));
    }

    @Test
    @DisplayName("Constructor should set user, message, and link")
    void constructor_setsFields() {
        User user = new User();
        Notification notification = new Notification(user, "Appointment confirmed", "/client/appointments");

        assertEquals(user, notification.getUser());
        assertEquals("Appointment confirmed", notification.getMessage());
        assertEquals("/client/appointments", notification.getLink());
    }

    @Test
    @DisplayName("isRead should default to false")
    void isRead_defaultsFalse() {
        Notification notification = new Notification();
        assertFalse(notification.getIsRead());
    }
}

class PostEntityTest {

    @Test
    @DisplayName("onCreate() should set createdAt to current time")
    void onCreate_setsCreatedAt() {
        Post post = new Post();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        post.onCreate();

        assertNotNull(post.getCreatedAt());
        assertTrue(post.getCreatedAt().isAfter(before));
    }

    @Test
    @DisplayName("isDeleted should default to false")
    void isDeleted_defaultsFalse() {
        Post post = new Post();
        assertFalse(post.getIsDeleted());
    }

    @Test
    @DisplayName("postMediaList should be initialised as empty, not null")
    void postMediaList_initialisedAsEmptyList() {
        Post post = new Post();
        assertNotNull(post.getPostMediaList());
        assertTrue(post.getPostMediaList().isEmpty());
    }
}

class MediaAssetEntityTest {

    @Test
    @DisplayName("onUpload() should set uploadedAt to current time")
    void onUpload_setsUploadedAt() {
        MediaAsset asset = new MediaAsset();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        asset.onUpload();

        assertNotNull(asset.getUploadedAt());
        assertTrue(asset.getUploadedAt().isAfter(before));
    }

    @Test
    @DisplayName("inPortfolio should default to false")
    void inPortfolio_defaultsFalse() {
        MediaAsset asset = new MediaAsset();
        assertFalse(asset.getInPortfolio());
    }

    @Test
    @DisplayName("postMediaList should be initialised as empty, not null")
    void postMediaList_initialisedAsEmptyList() {
        MediaAsset asset = new MediaAsset();
        assertNotNull(asset.getPostMediaList());
        assertTrue(asset.getPostMediaList().isEmpty());
    }
}

class PasswordResetTokenEntityTest {

    @Test
    @DisplayName("isExpired() should return false for a freshly created token")
    void isExpired_freshToken_returnsFalse() {
        User user = new User();
        PasswordResetToken token = new PasswordResetToken(user, "test-token");

        assertFalse(token.isExpired());
    }

    @Test
    @DisplayName("isExpired() should return true when expiresAt is in the past")
    void isExpired_pastExpiry_returnsTrue() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiresAt(LocalDateTime.now().minusHours(1));

        assertTrue(token.isExpired());
    }

    @Test
    @DisplayName("isExpired() should return false when expiresAt is in the future")
    void isExpired_futureExpiry_returnsFalse() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertFalse(token.isExpired());
    }

    @Test
    @DisplayName("Constructor should set expiresAt to 1 hour from now")
    void constructor_setsExpiryToOneHour() {
        User user = new User();
        LocalDateTime before = LocalDateTime.now().plusMinutes(59);
        LocalDateTime after = LocalDateTime.now().plusMinutes(61);

        PasswordResetToken token = new PasswordResetToken(user, "test-token");

        assertTrue(token.getExpiresAt().isAfter(before));
        assertTrue(token.getExpiresAt().isBefore(after));
    }

    @Test
    @DisplayName("used should default to false")
    void used_defaultsFalse() {
        PasswordResetToken token = new PasswordResetToken();
        assertFalse(token.getUsed());
    }

    @Test
    @DisplayName("Constructor should set user and token")
    void constructor_setsUserAndToken() {
        User user = new User();
        PasswordResetToken token = new PasswordResetToken(user, "abc123");

        assertEquals(user, token.getUser());
        assertEquals("abc123", token.getToken());
    }
}

class ProviderServiceOfferingEntityTest {

    private ServiceOffering serviceOffering(String price) {
        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setPriceEstimate(price);
        return serviceOffering;
    }

    @Test
    @DisplayName("getEffectivePrice() should return priceOverride when set")
    void getEffectivePrice_withOverride_returnsOverride() {
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setServiceOffering(serviceOffering("25.00"));
        providerServiceOffering.setPriceOverride("30.00");

        assertEquals("30.00", providerServiceOffering.getEffectivePrice());
    }

    @Test
    @DisplayName("getEffectivePrice() should return base price when no override set")
    void getEffectivePrice_noOverride_returnsBasePrice() {
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setServiceOffering(serviceOffering("25.00"));
        providerServiceOffering.setPriceOverride(null);

        assertEquals("25.00", providerServiceOffering.getEffectivePrice());
    }

    @Test
    @DisplayName("getEffectivePrice() should return override even when it is cheaper than base")
    void getEffectivePrice_overrideCheaperThanBase_returnsOverride() {
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setServiceOffering(serviceOffering("50.00"));
        providerServiceOffering.setPriceOverride("20.00");

        assertEquals("20.00", providerServiceOffering.getEffectivePrice());
    }

    @Test
    @DisplayName("isActive should default to true")
    void isActive_defaultsTrue() {
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        assertTrue(providerServiceOffering.getIsActive());
    }

    @Test
    @DisplayName("isDeleted should default to false")
    void isDeleted_defaultsFalse() {
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        assertFalse(providerServiceOffering.getIsDeleted());
    }

    @Test
    @DisplayName("bufferMinutes should default to 0")
    void bufferMinutes_defaultsZero() {
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        assertEquals(0, providerServiceOffering.getBufferMinutes());
    }

    @Test
    @DisplayName("Constructor should set provider and service offering")
    void constructor_setsProviderAndOffering() {
        Provider provider = new Provider();
        ServiceOffering serviceOffering = serviceOffering("25.00");
        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering(provider, serviceOffering);

        assertEquals(provider, providerServiceOffering.getProvider());
        assertEquals(serviceOffering, providerServiceOffering.getServiceOffering());
    }
}