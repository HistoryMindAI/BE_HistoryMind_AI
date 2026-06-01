package com.historymind.history_service.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EventDTOTest {

    @Test
    void testGettersAndSetters() {
        EventDTO dto = new EventDTO();
        dto.setId("evt_101");
        dto.setYear(1288);
        dto.setEvent("Trận Bạch Đằng");
        dto.setStory("Trần Hưng Đạo đánh tan quân Nguyên Mông");
        dto.setTone("heroic");
        dto.setTitle("Chiến thắng Bạch Đằng 1288");
        dto.setPersons(List.of("Trần Hưng Đạo"));
        dto.setPlaces(List.of("Bạch Đằng"));
        dto.setKeywords(List.of("bạch đằng", "trần hưng đạo"));

        assertEquals("evt_101", dto.getId());
        assertEquals(1288, dto.getYear());
        assertEquals("Trận Bạch Đằng", dto.getEvent());
        assertEquals("Trần Hưng Đạo đánh tan quân Nguyên Mông", dto.getStory());
        assertEquals("heroic", dto.getTone());
        assertEquals("Chiến thắng Bạch Đằng 1288", dto.getTitle());
        assertEquals(List.of("Trần Hưng Đạo"), dto.getPersons());
        assertEquals(List.of("Bạch Đằng"), dto.getPlaces());
        assertEquals(List.of("bạch đằng", "trần hưng đạo"), dto.getKeywords());
    }

    @Test
    void testEqualsAndHashCode() {
        EventDTO dto1 = new EventDTO();
        dto1.setId("evt_101");
        
        EventDTO dto2 = new EventDTO();
        dto2.setId("evt_101");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        EventDTO dto = new EventDTO();
        dto.setId("evt_101");
        dto.setYear(1288);

        String toStringStr = dto.toString();
        assertTrue(toStringStr.contains("evt_101"));
        assertTrue(toStringStr.contains("1288"));
    }
}
