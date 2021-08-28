<template>
  <Select @emitSelected="getSelectedData" :selectItems="items"/>
  <Upload :code="selectedCode"/>
</template>

<script>
import Select from "../components/DataRegister/Select.vue";
import Upload from "../components/DataRegister/Upload.vue";
export default {
  components: {
    Select,
    Upload,
  },
  data() {
    return {
      selectedCode: 1,
      items: [
        { text: "원본 업로드", code: 1 },
        { text: "수정본 업로드", code: 2 },
      ],
    };
  },
  methods: {
    getSelectedData(val) {
      this.selectedCode=val;
    },
  },
};
</script>
