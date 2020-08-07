package nextstep.subway.map.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.map.dto.PathResponse;
import nextstep.subway.station.dto.StationResponse;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.line.acceptance.step.LineAcceptanceStep.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.step.LineStationAcceptanceStep.지하철_노선에_지하철역_등록되어_있음;
import static nextstep.subway.map.acceptance.step.PathAcceptanceStep.출발역에서_도착역까지의_최단거리_경로_조회를_요청;
import static nextstep.subway.map.acceptance.step.PathAcceptanceStep.출발역에서_도착역까지의_최소_시간_경로_조회를_요청;
import static nextstep.subway.station.acceptance.step.StationAcceptanceStep.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 경로 검색")
public class PathAcceptanceTest extends AcceptanceTest {

    private Long lineId1;
    private Long lineId2;
    private Long lineId3;
    private Long stationId1;
    private Long stationId2;
    private Long stationId3;
    private Long stationId4;

    @BeforeEach
    public void setUp() {
        super.setUp();

        ExtractableResponse<Response> createLineResponse1 = 지하철_노선_등록되어_있음("2호선", "GREEN");
        ExtractableResponse<Response> createLineResponse2 = 지하철_노선_등록되어_있음("3호선", "ORANGE");
        ExtractableResponse<Response> createLineResponse3 = 지하철_노선_등록되어_있음("4호선", "BLUE");

        ExtractableResponse<Response> createdStationResponse1 = 지하철역_등록되어_있음("을지로4가");
        ExtractableResponse<Response> createdStationResponse2 = 지하철역_등록되어_있음("을지로3가");
        ExtractableResponse<Response> createdStationResponse3 = 지하철역_등록되어_있음("충무로역");
        ExtractableResponse<Response> createdStationResponse4 = 지하철역_등록되어_있음("동대문역");

        lineId1 = createLineResponse1.as(LineResponse.class).getId();
        lineId2 = createLineResponse2.as(LineResponse.class).getId();
        lineId3 = createLineResponse3.as(LineResponse.class).getId();

        stationId1 = createdStationResponse1.as(StationResponse.class).getId();
        stationId2 = createdStationResponse2.as(StationResponse.class).getId();
        stationId3 = createdStationResponse3.as(StationResponse.class).getId();
        stationId4 = createdStationResponse4.as(StationResponse.class).getId();

        지하철_노선에_지하철역_등록되어_있음(lineId1, null, stationId1, 0, 0);
        지하철_노선에_지하철역_등록되어_있음(lineId1, stationId1, stationId2, 2, 2);
        지하철_노선에_지하철역_등록되어_있음(lineId1, stationId2, stationId3, 2, 4);

        지하철_노선에_지하철역_등록되어_있음(lineId2, null, stationId2, 0, 0);
        지하철_노선에_지하철역_등록되어_있음(lineId2, stationId2, stationId4, 2, 2);

        지하철_노선에_지하철역_등록되어_있음(lineId3, null, stationId1, 0, 0);
        지하철_노선에_지하철역_등록되어_있음(lineId3, stationId1, stationId4, 2, 2);
        지하철_노선에_지하철역_등록되어_있음(lineId3, stationId4, stationId3, 3, 2);
    }

    /**
     * line1 : 을지로 3가 -> 충무로  2 + 2  = 4
     * line3 : 동대문 -> 충무로 -> 3 : 2 + 3  = 5
     */

    @DisplayName("최단 거리 검색")
    @Test
    void shortestDistance() {
        // when
        ExtractableResponse<Response> response = 출발역에서_도착역까지의_최단거리_경로_조회를_요청(1L, 3L);

        // then
        PathResponse pathResponse = response.as(PathResponse.class);


        List<Long> stationsIds = extractStationsIds(pathResponse);

        assertThat(pathResponse.getDistance()).isEqualTo(4);
        assertThat(stationsIds).containsExactlyElementsOf(Lists.newArrayList(1L, 2L, 3L));
        assertThat(pathResponse.getStationResponses()).extracting(StationResponse::getId).containsExactly(1L, 2L, 3L);
    }

    @DisplayName("최단 시간 검색")
    @Test
    void shortestDuration() {
        // when
        ExtractableResponse<Response> response = 출발역에서_도착역까지의_최소_시간_경로_조회를_요청(1L, 3L);

        // then
        PathResponse pathResponse = response.as(PathResponse.class);
        List<Long> stationsIds = extractStationsIds(pathResponse);

        assertThat(pathResponse.getDuration()).isEqualTo(4);
        assertThat(stationsIds).containsExactlyElementsOf(Lists.newArrayList(1L, 4L, 3L));
        assertThat(pathResponse.getStationResponses()).extracting(StationResponse::getId).containsExactly(1L, 4L, 3L);
    }

    private List<Long> extractStationsIds(PathResponse pathResponse) {
        return pathResponse.getStationResponses()
                .stream()
                .map(s -> s.getId())
                .distinct()
                .collect(Collectors.toList());
    }


}
