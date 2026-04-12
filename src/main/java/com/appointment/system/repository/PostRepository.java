package com.appointment.system.repository;

import com.appointment.system.entity.Post;
import com.appointment.system.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByProviderAndIsDeletedFalse(Provider provider);
}
