<template>
  <div class="container">
    <div class="select-box">
      <select class="form-control" v-model="selected" @click="onSelectData">
        <option v-for="item in items" :value="item.code" :key="item">
          {{ item.text }}
        </option>
      </select>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    selectItems: Array,
  },
  data() {
    return {
      selected: 1,
      items: this.selectItems,
    };
  },
  methods: {
    onSelectData() {
      this.$emit("emitSelected", this.selected);
    },
  },
};
</script>

<style lang="scss" scoped>
.container {
  margin-top: 50px;
  display: flex;
  > * {
    font-size: 15px;
    margin-right: 10px;
    &:last-child {
      margin-right: 0;
    }
  }
  .select-box {
    display: flex;
    select {
      width: 150px;
      height: 50px;
    }
  }
}
</style>