<template>
  <div>
    <!-- <div style="padding-top:200px;"></div> -->
    <b-button variant="light" style="position: absolute; left: 0; z-index: 3; margin-top: 4%; margin-left: 3%"
      @click="openLeft">
      <b-icon icon="search"></b-icon>
    </b-button>
    <house-left @focusDong="focusingDong" :style="
      !leftClick
        ? 'display:none;'
        : 'position:absolute; left:0; margin-top:3%; margin-bottom:3%; margin-left:2%; background-color:white; height:80%; z-index:2; overflow-y:auto;'
    "></house-left>
    <house-right v-if="loadViewRendering" :aptInfomation="aptInfomation" :style="
      !rightClick
        ? 'display:none;'
        : 'position:absolute; padding:20px; width:500px; right:0; margin-top:3%; margin-bottom:3%; margin-right:2%; background-color:white; height:80%; z-index:2; overflow-y:auto;'
    ">
    </house-right>
    <b-button id="title" variant="light" style="position: absolute; bottom: 0; z-index: 3" @click="closeAll"
      v-if="this.rightClick || this.leftClick">
      Close All</b-button>
    <b-button v-if="aptInfomation != null" variant="light" class="m-5"
      style="position: absolute; right: 0; z-index: 3; margin-top: 4%; margin-right: 3%" @click="openRight">
      <b-icon icon="info-circle-fill
"></b-icon>
    </b-button>
    <div id="map" style="position: absolute; z-index: 1"></div>
  </div>
</template>

<script>
import http from "@/api/http";
import HouseLeft from "@/components/house/HouseLeft.vue";
import HouseRight from "@/components/house/HouseRight.vue";
import { mapActions } from "vuex";

export default {
  name: "KakaoMap",
  components: {
    HouseLeft,
    HouseRight,
  },
  data() {
    return {
      map: null,
      leftClick: false,
      rightClick: false,
      loadViewRendering: false,
      focusDong: {
        name: "",
        code: null,
        budgetLow: null,
        budgetHigh: null,
        areaLow: null,
        areaHigh: null,
        aptName: null,
      },
      markers: [],
      apt: [],
      clusterer: null,

      // 마커 이미지 변경
      imageSrc: "",
      imageSize: "",
      imageOption: "",
      markerImage: "",

      aptInfomation: null,
    };
  },
  computed: {},
  mounted() {
    if (!window.kakao || !window.kakao.maps) {
      const script = document.createElement("script");
      script.src = `//dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=${process.env.VUE_APP_KAKAOMAP_KEY}&libraries=services,clusterer`;
      /* global kakao */
      script.addEventListener("load", () => {
        kakao.maps.load(this.initMap);
      });
      document.head.appendChild(script);
    } else {
      this.initMap();
    }
  },
  methods: {
    ...mapActions(["setHouseListStore", "setDetailHouseStore", "getAptGrade"]),
    initMap() {
      const container = document.getElementById("map");
      const options = {
        center: new kakao.maps.LatLng(37.5666805, 126.9784147),
        level: 3,
      };

      //지도 객체를 등록합니다.
      //지도 객체는 반응형 관리 대상이 아니므로 initMap에서 선언합니다.
      this.map = new kakao.maps.Map(container, options);

      this.clusterer = new kakao.maps.MarkerClusterer({
        map: this.map, // 마커들을 클러스터로 관리하고 표시할 지도 객체
        averageCenter: true, // 클러스터에 포함된 마커들의 평균 위치를 클러스터 마커 위치로 설정
        minLevel: 3, // 클러스터 할 최소 지도 레벨
        gridSize: 80,
      });

      // 마커 이미지
      this.imageSrc = require("@/assets/apticon.png"); // 마커이미지의 주소입니다
      this.imageSize = new kakao.maps.Size(64, 69); // 마커이미지의 크기입니다
      this.imageOption = { offset: new kakao.maps.Point(27, 69) }; // 마커이미지의 옵션입니다. 마커의 좌표와 일치시킬 이미지 안에서의 좌표를 설정합니다
      this.markerImage = new kakao.maps.MarkerImage(
        this.imageSrc,
        this.imageSize,
        this.imageOption
      );

      // 지도에 확대 축소 컨트롤을 생성한다
      var zoomControl = new kakao.maps.ZoomControl();

      // 지도의 우측에 확대 축소 컨트롤을 추가한다
      this.map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

      this.zoomMarkers();
    },

    // 사이드 컴포넌트 열기/닫기
    openLeft() {
      this.leftClick = !this.leftClick;
    },
    // 버튼 클릭 시 오른쪽 컴포넌트 열기
    openRight() {
      this.loadViewRendering = true;
      this.rightClick = !this.rightClick;
    },
    // 마커 클릭 시 오른쪽 컴포넌트 열기
    openRightCom() {
      if (this.loadViewRendering == false) {
        this.loadViewRendering = true;
      }
      this.rightClick = true;
    },
    closeAll() {
      this.leftClick = false;
      this.rightClick = false;
    },

    zoomMarkers() {
      const vueInstance = this;
      vueInstance.createMarkers();
      // 지도가 확대 또는 축소되면 마지막 파라미터로 넘어온 함수를 호출하도록 이벤트를 등록합니다
      kakao.maps.event.addListener(this.map, "zoom_changed", function () {
        // 지도의 현재 레벨을 얻어옵니다
        var level = vueInstance.map.getLevel();

        // var message = "현재 지도 레벨은 " + level + " 입니다";
        // console.log(message);

        if (level <= 4) {
          vueInstance.createMarkers();
        } else {
          vueInstance.clusterer.clear();
          vueInstance.markers = [];
        }
      });

      kakao.maps.event.addListener(this.map, "dragend", function () {
        // 지도 중심좌표를 얻어옵니다
        // var latlng = vueInstance.map.getCenter();

        vueInstance.createMarkers();

        // var message = "변경된 지도 중심좌표는 " + latlng.getLat() + " 이고, ";
        // message += "경도는 " + latlng.getLng() + " 입니다";
        // console.log(message);
      });
    },

    async createMarkers() {
      var neLat = this.map.getBounds().getNorthEast().getLat();
      var neLng = this.map.getBounds().getNorthEast().getLng();
      var swLat = this.map.getBounds().getSouthWest().getLat();
      var swLng = this.map.getBounds().getSouthWest().getLng();

      await http
        .get(`/houses?minLat=${swLat}&minLng=${swLng}&maxLat=${neLat}&maxLng=${neLng}`)
        .then(({ data }) => {
          this.apt = data;
        });

      this.setHouseListStore(this.apt);

      this.clusterer.clear();
      this.markers = [];
      for (let i = 0; i < this.apt.length; i++) {
        // 마커가 표시될 위치입니다
        let markerPosition = new kakao.maps.LatLng(this.apt[i].lat, this.apt[i].lng);

        // 마커를 생성합니다
        let marker = new kakao.maps.Marker({
          position: markerPosition,
          image: this.markerImage,
        });
        this.markers.push(marker);

        const vueInstance = this;
        kakao.maps.event.addListener(marker, "click", function () {
          vueInstance.openRightCom();
          vueInstance.aptInfomation = vueInstance.apt[i];
          vueInstance.setDetailHouseStore(vueInstance.apt[i]);
          console.log(vueInstance.aptInfomation);
          vueInstance.map.setCenter(markerPosition);

          // aptGrade store setting
          console.log('click 요청 ' + vueInstance.apt[i].aptCode, vueInstance.apt[i].lat, vueInstance.aptInfomation.lng);
          vueInstance.getAptGrade(vueInstance.apt[i]);
        });
      }
      this.clusterer.addMarkers(this.markers);
    },

    // 검색한 곳에서 데이터 받아오기
    focusingDong(dong) {
      this.rightClick = false;
      this.focusDong.name = dong.dongAdd;
      this.focusDong.code = dong.dongCode;
      this.focusDong.budgetLow = dong.budgetLow;
      this.focusDong.budgetHigh = dong.budgetHigh;
      this.focusDong.areaLow = dong.areaLow;
      this.focusDong.areaHigh = dong.areaHigh;
      this.focusDong.aptName = dong.aptName;
      this.focusDongCreate();
    },

    focusDongCreate() {
      // 장소 검색 객체를 생성합니다
      var ps = new kakao.maps.services.Places();

      const vueInstance = this;

      // 키워드로 장소를 검색합니다
      ps.keywordSearch(this.focusDong.name, placesSearchCB);

      // 키워드 검색 완료 시 호출되는 콜백함수 입니다
      function placesSearchCB(data, status) {
        if (status === kakao.maps.services.Status.OK) {
          // 검색된 장소 위치를 기준으로 지도 범위를 재설정하기위해
          // LatLngBounds 객체에 좌표를 추가합니다
          var bounds = new kakao.maps.LatLngBounds();

          for (var i = 0; i < data.length; i++) {
            // displayMarker(data[i]);
            bounds.extend(new kakao.maps.LatLng(data[i].y, data[i].x));
          }

          // 검색된 장소 위치를 기준으로 지도 범위를 재설정합니다
          vueInstance.map.setBounds(bounds);
        }
      }
      this.aptInfo();
    },

    async aptInfo() {
      await http
        .get(
          `/houses?dongCode=${this.focusDong.code}&apartmentName=${this.focusDong.aptName}&budgetLow=${this.focusDong.budgetLow}&budgetHigh=${this.focusDong.budgetHigh}&areaLow=${this.focusDong.areaLow}&areaHigh=${this.focusDong.areaHigh}`
        )
        .then(({ data }) => {
          this.apt = data;
          console.log(this.apt);
        });

      this.setHouseListStore(this.apt);

      this.clusterer.clear();
      this.markers = [];
      for (let i = 0; i < this.apt.length; i++) {
        // 마커가 표시될 위치입니다
        let markerPosition = new kakao.maps.LatLng(this.apt[i].lat, this.apt[i].lng);

        // 마커를 생성합니다
        let marker = new kakao.maps.Marker({
          position: markerPosition,
          image: this.markerImage,
        });
        this.markers.push(marker);

        const vueInstance = this;
        kakao.maps.event.addListener(marker, "click", function () {
          vueInstance.openRightCom();
          vueInstance.aptInfomation = vueInstance.apt[i];
          vueInstance.setDetailHouseStore(vueInstance.apt[i]);
          console.log(vueInstance.aptInfomation);
          vueInstance.map.setCenter(markerPosition);
        });
      }
      this.clusterer.addMarkers(this.markers);
    },
    // 배열에 추가된 마커들을 지도에 표시하거나 삭제하는 함수입니다
    setMarkers() {
      for (var i = 0; i < this.markers.length; i++) {
        this.markers[i].setMap(this.map);
      }
    },
  },
};
</script>

<style scoped>
#map {
  width: 100%;
  height: calc(100% - 62px);
}
</style>
