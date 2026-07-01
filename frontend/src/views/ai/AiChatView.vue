<template>
  <div class="container">
    <div id="chatSection">
      <h1>AI 채팅</h1>
      <div>
        <input
          id="simple"
          type="radio"
          class="form-check-input"
          name="endpoint"
          value="simple"
          checked
        >
        <label
          for="simple"
          class="form-check-label"
        >Simple</label>
        <input
          id="memory"
          type="radio"
          class="form-check-input"
          name="endpoint"
          value="memory"
        >
        <label
          for="memory"
          class="form-check-label"
        >memory</label>
        <input
          id="userId"
          type="text"
          name="userId"
          placeholder="사용자id"
          value="ssafy"
        >
        <input
          id="tool"
          type="radio"
          class="form-check-input"
          name="endpoint"
          value="tool"
        >
        <label
          for="tool"
          class="form-check-label"
        >tool</label>
        <input
          id="rag"
          type="radio"
          class="form-check-input"
          name="endpoint"
          value="rag"
        >
        <label
          for="member"
          class="form-check-label"
        >RAG</label>
      </div>
      <div id="chatHistory" />
      <input
        id="message"
        type="text"
        name="message"
        required
        placeholder="대화를 전송하려면 enter"
      >
    </div>
  </div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
#main {
    display: flex;
    height: 100vh;
    margin: 0;
}

#chatSection {
    flex: 1;
    border-right: 1px solid #ccc;
    padding: 10px;
    display: flex;
    flex-direction: column;
}

#todoSection {
    flex: 2;
    padding: 10px;
}

#chatHistory {
    border: 1px solid #ccc;
    padding: 10px;
    flex-grow: 1;
    overflow-y: scroll;
    margin-bottom: 10px;
    height: calc(100vh - 420px);
}

.message {
    margin: 5px 0;
}

.user {
    text-align: right;
    color: blue;
}

.bot {
    text-align: left;
    color: green;
}

#todoList {
    border: 1px solid #ccc;
    padding: 10px;
    height: 300px;
    overflow-y: scroll;
    margin-bottom: 10px;
}

#chatForm {
    display: flex;
    align-items: center;
}

#message {
    flex-grow: 1;
    margin-right: 10px;
    padding: 5px;
}
</style>
