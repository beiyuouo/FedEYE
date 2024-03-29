
<template>
  <div
    tabindex="-1"
    class="super-flow__node"
    :class="{'can-move': nodeDrop}"
    :style="style"
    @mousedown.left="nodeMousedown"
    @mouseenter="nodeMouseenter"
    @mouseleave="nodeMouseleave"
    @mouseup="nodeMouseup"
    @contextmenu.prevent.stop="nodeContextmenu">
    <slot :meta="node.meta"></slot>
    <div
      v-for="(dir, key) in direction"
      v-show="output && lineDrop"
      :class="`node-side node-side-${key}`"
      @contextmenu.stop.prevent
      @mousedown.left.prevent.stop="evt => sideMousedown(evt, dir)">
    </div>
  </div>
</template>

<script>

  import {
    direction
  } from './types'

  import {
    getOffset
  } from './utils'

  export default {
    props: {
      graph: Object,
      node: Object,
      index: Number,
      isMove: Boolean,
      isTemEdge: Boolean,
      nodeIntercept: Function,
      lineDrop: Boolean,
      nodeDrop: Boolean
    },
    data() {
      return {
        direction,
        output: this.nodeIntercept()
      }
    },
    computed: {
      style() {
        const {
          position,
          width,
          height
        } = this.node

        return {
          width: width + 'px',
          height: height + 'px',
          top: position[1] + 'px',
          left: position[0] + 'px'
        }
      }
    },
    methods: {

      nodeMousedown(evt) {
        this.graph.toLastNode(this.index)
        this.$emit('node-mousedown', this.node, getOffset(evt))
      },

      nodeMouseenter(evt) {
        this.output = this.nodeIntercept()
        this.graph.mouseonNode = this.node
        if (!this.isTemEdge) return
        this.$emit('node-mouseenter', evt, this.node, getOffset(evt, this.$el))
      },

      nodeMouseleave() {
        this.graph.mouseonNode = null
        if (!this.isTemEdge) return
        this.$emit('node-mouseleave')
      },

      nodeMouseup() {
        if (!this.isTemEdge) return
        this.$emit('node-mouseup')
      },

      nodeContextmenu(evt) {
        this.$emit('node-contextmenu', evt, this.node)
      },

      sideMousedown(evt) {
        this.$emit('side-mousedown', evt, this.node, getOffset(evt, this.$el))
      }
    }
  }
</script>

<style lang="scss">
  .super-flow__node {
    // box-shadow       : 1px 2px 8px rgba(0, 0, 0, .2);
    user-select      : none;

    position         : absolute;
    background-color : #ffffff;
    border           : 1px solid rgb(180, 180, 180);
    z-index          : 1;
    outline          : none;

    &.can-move {
      cursor : move;
    }

    &-header {
      background-color : green;
    }

    &-body {

    }

    .node-side {
      position : absolute;
      cursor   : crosshair;

      &-top {
        top    : -5px;
        right  : 0;
        left   : 0;
        height : 10px;
      }

      &-right {
        top    : 0;
        right  : -5px;
        bottom : 0;
        width  : 10px;
      }

      &-bottom {
        right  : 0;
        bottom : -5px;
        left   : 0;
        height : 10px;
      }

      &-left {
        top    : 0;
        bottom : 0;
        left   : -5px;
        width  : 10px;
      }
    }

    &:hover {
      // box-shadow : 1px 2px 8px rgba(0, 0, 0, .4);
    }

    &.isSelect {
      z-index : 2;
    }
  }
</style>
