<template>
  <div class="uploading-data">
    <span
      :class="{
        'status-canceled': status == 'canceled',
      }"
    >
      {{ file.name }} / {{ file.size }} byte</span
    >
    <br />
    <small v-if="status == 'success'">업로드 완료</small>
    <small v-else-if="status == 'retrying'">재업로드 중...</small>
    <small v-else-if="status == 'error'">업로드 실패</small>
    <small v-else-if="status == 'canceled'">업로드 취소됨</small>
    <!-- <small v-else>uploading {{ uploadedAmount }}%</small> -->
    <div class="progress" v-else>
      <div class="progress-count">{{ uploadedAmount }}%</div>
      <div class="progress-bar" :style="uploadStyle"></div>
    </div>

    <div v-if="uploadedAmount<100">
      <span v-if="isUploading">
        <button @click="isPaused ? resume() : pause()">
          {{ isPaused ? "재업로드" : "일시정지" }}
        </button>
        <button @click="cancel()">취소</button>
      </span>
    </div>
    <small v-else>업로드 완료됨</small>
  </div>
</template>

<script>
export default {
  props: ["file", "status", "progress"],
  data() {
    return {
      isPaused: false, // we upload straight away by default
    };
  },
  computed: {
    isUploading() {
      return this.status !== "success" && this.status !== "canceled";
    },
    uploadedAmount() {
      return (this.progress * 100).toFixed(2);
    },
    uploadStyle() {
      return `
       width: ${this.uploadedAmount}%
     `;
    },
  },
  methods: {
    upload() {
      this.file.flowObj.resume();
      this.isPaused = false;
    },
    pause() {
      this.file.pause();
      this.isPaused = true;
    },
    resume() {
      this.pause();
      this.upload();
    },
    cancel() {
      this.$emit("cancel", this.file);
    },
  },
};
</script>

<style lang="scss" scoped>
@import "../../assets/css/main.scss";

.uploading-data {
  border-bottom: 1px solid #eee;
  padding: 20px;
  &:first-child {
    border-top: 1px solid #eee;
  }
  .status-canceled {
    text-decoration: line-through;
  }
  .progress {
    position: relative;
    .progress-count {
      position: absolute;
      width: 100%;
      text-align: center;
      color: #fff;
    }
    .progress-bar{
        background: $mint;
    }
  }
}
</style>
