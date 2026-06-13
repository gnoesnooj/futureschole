package com.futureschole.eventpipeline.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    PAGE_VIEW,
    SEARCH,
    PURCHASE,
    ERROR;
}
