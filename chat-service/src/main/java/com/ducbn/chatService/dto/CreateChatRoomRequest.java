package com.ducbn.chatService.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
    private Long customerId;
    private String customerName;
    private Long busOperatorId;
    private String busOperatorName;
    private Long busRouteId;
    private String routeName;
}

