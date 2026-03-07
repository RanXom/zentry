package com.ranxom.zentry.repository;

import com.ranxom.zentry.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByKey(String key);

}
