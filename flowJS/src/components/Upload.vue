<template>
  <div class="container">
    <div class="upload">
      <!-- 원본 업로드 -->
      <div v-if="code === 1">
        <p>원본 업로드</p>
        <div class="btn btn-select" @click="onCategoryModal = true">
          카테고리 선택
        </div>
        <div class="btn btn-select" @click.self="onUploadMenu = !onUploadMenu">
          업로드 선택
          <div class="uploadMenu" v-if="onUploadMenu === true">
            <ul>
              <li @click="onFolderpicker">
                <input
                  type="file"
                  ref="folderpicker"
                  webkitdirectory
                  multiple
                />
                <label for="folderpicker">폴더 선택</label>
              </li>
              <li @click="onFilepicker">
                <input type="file" ref="filepicker" multiple />
                <label for="filepicker">파일 선택</label>
              </li>
            </ul>
          </div>
        </div>

        <div class="btn btn-select btn-upload" @click="onFileUpload">
          파일업로드 시작하기
        </div>
      </div>

      <!-- 수정본 업로드 -->
      <div v-else-if="code === 2">
        <p>수정본 업로드</p>
        <div class="btn btn-select" @click="onCategoryModal = true">
          카테고리 선택
        </div>
      </div>

      <small>카테고리명: {{ category }}</small>
      <div class="file-list">
        <UploadingData
          v-for="(file, index) in files"
          v-bind:key="file.file.uniqueIdentifier + index"
          :file="file.file"
          :status="file.status"
          :progress="file.progress"
          @cancel="cancelFile"
        />
      </div>
    </div>
    <!-- upload end -->
  </div>
  <!-- container end -->

  <!-- Modal -->
  <div class="modal-bg" v-if="onCategoryModal === true">
    <div class="modal-content">
      <div class="modal-header">
        <button
          type="button"
          class="close btn"
          @click="onCategoryModal = false"
        >
          <img src="../../assets/images/close.svg" alt="close icon" />
        </button>
      </div>
      <div class="modal-body">
        엔터 누르면 됩니다.
        <input
          type="text"
          v-model="category"
          @keyup.enter="onCategoryModal = false"
        />
      </div>
    </div>
  </div>
</template>

<script>
import Flow from "@flowjs/flow.js";
import UploadingData from "./UploadingData.vue";
// import { BASE_URL } from "../../api/baseUrl";

export default {
  components: {
    UploadingData,
  },
  props: {
    code: Number,
  },
  data() {
    return {
      onCategoryModal: false,
      onUploadMenu: false,
      category: "",
      flow: null,
      files: [],
    };
  },
  mounted() {},
  watch: {
    category: function (newValue, oldValue) {
      if (newValue !== oldValue) {
        this.flow = new Flow({
          target: `http://192.168.0.3:8080/upload`,
          // target:'http://localhost:8080/upload',
          chunkSize: 1024 * 1024,
          testChunks: false,
          headers: { category: encodeURIComponent(this.category) },
        });

        // this.flow.on("catchAll", function () {
        //   console.log.apply(console, arguments);
        // });

        this.flow.on("fileAdded", (file) => {
          this.files.push({
            file,
            status: "uploading",
            progress: 0,
          });
        });

        this.flow.on("fileProgress", (file) => {
          let localFile = this.findFile(file);
          let progress = file.progress();
          if (progress > localFile.progress) localFile.progress = progress;
        });

        if (!this.flow.support) return alert("지원하지 않는 브라우저입니다.");
      }
    },
  },
  methods: {
    onUploadOption() {},
    onFolderpicker() {
      this.$refs.folderpicker.click();
      this.onUploadMenu = false;

      this.flow.assignBrowse(this.$refs.folderpicker);
    },
    onFilepicker() {
      this.$refs.filepicker.click();
      this.onUploadMenu = false;

      this.flow.assignBrowse(this.$refs.filepicker);
    },
    onFileUpload() {
      if (this.category === "" || this.category === null)
        return alert("카테고리를 입력해주세요");

      // console.log(this.flow);
      this.flow.upload();
    },
    findFile(file) {
      return (
        this.files.find(
          (item) =>
            item.file.uniqueIdentifier === file.uniqueIdentifier &&
            item.status !== "canceled"
        ) ?? {}
      );
    },
    cancelFile(file) {
      this.findFile(file).status = "canceled";
      file.cancel();
    },
  },
};
</script>

<style lang="scss" scoped>
@import "../../assets/css/main.scss";
.container {
  .upload {
    border-top: 4px solid $primary !important;
    border: 1px solid rgba(0, 0, 0, 0.125);
    padding: 20px;
    div {
      .btn-select {
        border: 1px solid rgba(0, 0, 0, 0.125);
        border-radius: 0;
        background: #f8f8f8;
        margin-right: 12px;
        position: relative;
        &.btn-upload {
          float: right;
        }

        .uploadMenu {
          position: absolute;
          top: 38px;
          left: -1px;
          border: 1px solid rgba(0, 0, 0, 0.125);
          background: #fff;
          z-index: 3000;
          ul {
            margin: 0;
            padding: 0;
            li {
              width: 150px;
              height: 28px;
              line-height: 28px;
              list-style: none;
              input[type="file"] {
                /* 파일 필드 숨기기 */
                position: absolute;
                width: 1px;
                height: 1px;
                padding: 0;
                margin: -1px;
                overflow: hidden;
                clip: rect(0, 0, 0, 0);
                border: 0;
              }
              input[type="file"] + label {
                cursor: pointer;
                width: 100%;
              }
              &:hover {
                background: rgba(0, 0, 0, 0.125);
              }
            }
          }
        }
      }
    }

    .file-list {
      height: 300px;
      max-height: 600px;
      overflow-y: scroll;
      border: 1px solid rgba(0, 0, 0, 0.125);
      box-sizing: content-box;
    }
  }
}

/* modal */
.modal-bg {
  width: 100%;
  height: 100%;
  position: fixed;
  padding: 20px;
  background: rgba(0, 0, 0, 0.5);
  top: 0;
  left: 0;
  z-index: 3010;
  display: flex;
  justify-content: center;
  align-items: center;

  .modal-content {
    width: 600px;
    max-height: 400px;
    background: white;
    border-radius: 4px;
    padding: 20px;
    .modal-header {
      position: relative;
      .close {
        position: absolute;
        top: 0;
        right: 0;
        padding: 0;
        margin: 0;
      }
    }
  }
}
</style>