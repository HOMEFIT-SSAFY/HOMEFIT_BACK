package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.config.OpenApiProperties;
import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyType;
import java.util.List;
import org.junit.jupiter.api.Test;

class RtmsOpenApiClientTest {

    private final RtmsOpenApiClient client = new RtmsOpenApiClient(new OpenApiProperties());

    @Test
    void parsesOfficetelComplexNameFromDanjiNameField() {
        String xml = """
                <response>
                  <body>
                    <items>
                      <item>
                        <년>2026</년>
                        <월>6</월>
                        <일>23</일>
                        <법정동>역삼동</법정동>
                        <단지명>역삼동하나빌</단지명>
                        <전용면적>30.73</전용면적>
                        <층>4</층>
                        <지번>1008</지번>
                      </item>
                    </items>
                  </body>
                </response>
                """;

        List<PropertyDeal> deals = client.parse(xml, PropertyType.OFFICETEL, PropertyDealType.RENT, "11680", "202606");

        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).propertyName()).isEqualTo("역삼동하나빌");
    }

    @Test
    void parsesOfficetelComplexNameFromOffiNmField() {
        String xml = """
                <response>
                  <body>
                    <items>
                      <item>
                        <buildYear>2007</buildYear>
                        <dealAmount>110,000</dealAmount>
                        <dealDay>21</dealDay>
                        <dealMonth>12</dealMonth>
                        <dealYear>2015</dealYear>
                        <excluUseAr>152.95</excluUseAr>
                        <floor>12</floor>
                        <jibun>24</jibun>
                        <offiNm>르메이에르종로타운1</offiNm>
                        <sggCd>11110</sggCd>
                        <sggNm>종로구</sggNm>
                        <umdNm>종로1가</umdNm>
                      </item>
                    </items>
                  </body>
                </response>
                """;

        List<PropertyDeal> deals = client.parse(xml, PropertyType.OFFICETEL, PropertyDealType.TRADE, "11110", "201512");

        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).propertyName()).isEqualTo("르메이에르종로타운1");
    }

    @Test
    void fallsBackToAddressBasedNameWhenBuildingNameIsMissing() {
        String xml = """
                <response>
                  <body>
                    <items>
                      <item>
                        <년>2026</년>
                        <월>6</월>
                        <일>23</일>
                        <법정동>풍무동</법정동>
                        <주택유형>단독다가구</주택유형>
                        <전용면적>25.83</전용면적>
                        <층>9</층>
                        <지번>6871-53</지번>
                      </item>
                    </items>
                  </body>
                </response>
                """;

        List<PropertyDeal> deals = client.parse(xml, PropertyType.ONEROOM, PropertyDealType.RENT, "41570", "202606");

        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).propertyName()).isEqualTo("풍무동 6871-53 원룸");
    }
}
