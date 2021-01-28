package com.server.bluedotproject.dto.response;

import com.server.bluedotproject.entity.enumclass.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UserRoleApiResponse {
    private Long userId;

    private RoleName userRole;
}
