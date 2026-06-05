package com.appointment.system.controller;

import com.appointment.system.config.GlobalModelAttributes;
import com.appointment.system.config.SecurityConfig;
import com.appointment.system.entity.Provider;
import com.appointment.system.entity.ProviderServiceOffering;
import com.appointment.system.entity.ServiceOffering;
import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.BookingType;
import com.appointment.system.enums.Role;
import com.appointment.system.enums.ServiceCategory;
import com.appointment.system.repository.ProviderServiceOfferingRepository;
import com.appointment.system.repository.UserRepository;
import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.service.NotificationService;
import com.appointment.system.service.ServiceOfferingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceOfferingController.class)
@Import({GlobalModelAttributes.class, SecurityConfig.class})
@WithMockUser
@AutoConfigureMockMvc(addFilters = false)
class ServiceOfferingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ServiceOfferingService serviceOfferingService;
    @MockitoBean
    private ProviderServiceOfferingRepository providerServiceOfferingRepository;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private UserRepository userRepository;

    private CustomUserDetails providerUser;

    @BeforeEach
    void setUp() {
        Provider person = new Provider();
        person.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setUsername("provider@test.com");
        user.setEmail("provider@test.com");
        user.setPassword("password");
        user.setRole(Role.ROLE_PROVIDER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPerson(person);

        providerUser = new CustomUserDetails(user);
    }

    private ProviderServiceOffering buildMockOffering() {
        ServiceOffering serviceOffering = new ServiceOffering();
        serviceOffering.setTitle("Haircut");
        serviceOffering.setDefaultDuration(60);
        serviceOffering.setPriceEstimate("25.00");
        serviceOffering.setCategory(ServiceCategory.HAIR);
        serviceOffering.setBookingType(BookingType.SLOT_BASED);

        ProviderServiceOffering providerServiceOffering = new ProviderServiceOffering();
        providerServiceOffering.setId(1L);
        providerServiceOffering.setServiceOffering(serviceOffering);
        providerServiceOffering.setBufferMinutes(0);
        return providerServiceOffering;
    }

//    @Test
//    @DisplayName("GET /provider/services should return services list page")
//    void listServices_authenticated_returnsView() throws Exception {
//        when(serviceOfferingService.getProviderOfferings(1L)).thenReturn(List.of());
//        when(serviceOfferingService.getAllCategories()).thenReturn(List.of(ServiceCategory.values()));
//
//        mockMvc.perform(get("/provider/services").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/services"))
//                .andExpect(model().attributeExists("services"))
//                .andExpect(model().attributeExists("createRequest"))
//                .andExpect(model().attributeExists("categories"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/services/create should redirect on valid service creation")
//    void createService_validRequest_redirectsWithSuccess() throws Exception {
//        doNothing().when(serviceOfferingService).createAndAssign(anyLong(), any());
//
//        mockMvc.perform(post("/provider/services/create").with(user(providerUser)).with(csrf())
//                        .param("title", "Haircut")
//                        .param("defaultDuration", "60")
//                        .param("priceEstimate", "25.00")
//                        .param("category", "HAIR")
//                        .param("bookingType", "SLOT_BASED"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/services"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/services/create should stay on page when validation fails")
//    void createService_invalidRequest_staysOnPage() throws Exception {
//        when(serviceOfferingService.getProviderOfferings(1L)).thenReturn(List.of());
//        when(serviceOfferingService.getAllCategories()).thenReturn(List.of(ServiceCategory.values()));
//
//        mockMvc.perform(post("/provider/services/create").with(user(providerUser)).with(csrf())
//                        .param("title", "")
//                        .param("defaultDuration", "60")
//                        .param("category", "HAIR"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/services"));
//    }
//
//    @Test
//    @DisplayName("GET /provider/services/edit/{id} should return edit form with prefilled data")
//    void editPage_existingService_returnsEditView() throws Exception {
//        ProviderServiceOffering providerServiceOffering = buildMockOffering();
//        when(providerServiceOfferingRepository.findById(1L)).thenReturn(java.util.Optional.of(providerServiceOffering));
//        when(serviceOfferingService.getAllCategories()).thenReturn(List.of(ServiceCategory.values()));
//
//        mockMvc.perform(get("/provider/services/edit/1").with(user(providerUser)))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/service-edit"))
//                .andExpect(model().attributeExists("editRequest"))
//                .andExpect(model().attributeExists("serviceId"))
//                .andExpect(model().attributeExists("categories"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/services/edit/{id} should redirect on valid update")
//    void updateService_validRequest_redirectsWithSuccess() throws Exception {
//        doNothing().when(serviceOfferingService).update(anyLong(), anyLong(), any());
//
//        mockMvc.perform(post("/provider/services/edit/1").with(user(providerUser)).with(csrf())
//                        .param("title", "Updated Haircut")
//                        .param("defaultDuration", "45")
//                        .param("priceEstimate", "30.00")
//                        .param("category", "HAIR")
//                        .param("bookingType", "SLOT_BASED"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/services"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/services/edit/{id} should stay on page when validation fails")
//    void updateService_invalidRequest_staysOnPage() throws Exception {
//        when(serviceOfferingService.getAllCategories()).thenReturn(List.of(ServiceCategory.values()));
//
//        mockMvc.perform(post("/provider/services/edit/1").with(user(providerUser)).with(csrf())
//                        .param("title", "")
//                        .param("defaultDuration", "45")
//                        .param("category", "HAIR"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("provider/service-edit"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/services/deactivate/{id} should redirect on success")
//    void deactivateService_owner_redirectsWithSuccess() throws Exception {
//        doNothing().when(serviceOfferingService).deactivate(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/services/deactivate/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/provider/services"))
//                .andExpect(flash().attributeExists("successMessage"));
//    }
//
//    @Test
//    @DisplayName("POST /provider/services/deactivate/{id} should propagate SecurityException when not owner")
//    void deactivateService_notOwner_throws() throws Exception {
//        doThrow(new SecurityException("Not authorized")).when(serviceOfferingService).deactivate(anyLong(), anyLong());
//
//        mockMvc.perform(post("/provider/services/deactivate/1").with(user(providerUser)).with(csrf()))
//                .andExpect(status().is5xxServerError());
//    }
}