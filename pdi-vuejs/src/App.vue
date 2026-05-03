<template>
  <main class="container">
    <h1>Processador de Imagens</h1>

    <section class="formulario">
      <input type="file" multiple accept="image/*" @change="selecionarArquivos" />

      <select v-model="operacao">
        <option value="salt-pepper">Salt and Pepper</option>
        <option value="redimensionar">Redimensionar</option>
        <option value="rotacionar">Rotacionar</option>
        <option value="sobel">Sobel</option>
        <option value="laplace-gaussiano">Laplace Gaussiano</option>
        <option value="prewitt-duplo">Prewitt Duplo</option>
      </select>

      <input
        v-if="operacao === 'salt-pepper'"
        v-model="nivelRuido"
        type="number"
        step="0.01"
        min="0.01"
        max="1"
        placeholder="Nível do ruído"
      />

      <input
        v-if="operacao === 'redimensionar'"
        v-model="escala"
        type="number"
        min="1"
        max="500"
        placeholder="Escala em %"
      />

      <input
        v-if="operacao === 'rotacionar'"
        v-model="angulo"
        type="number"
        placeholder="Ângulo"
      />

      <button @click="processar" :disabled="arquivos.length === 0 || carregando">
        {{ carregando ? 'Processando...' : 'Processar' }}
      </button>

      <p v-if="erro" class="erro">{{ erro }}</p>
    </section>

    <section class="galeria">
      <div v-for="imagem in imagens" :key="imagem.nome" class="card">
        <h3>{{ imagem.nome }}</h3>

        <p>Original</p>
        <img :src="imagem.original" alt="Imagem original" />

        <template v-if="imagem.processada">
          <p>Processada</p>
          <img :src="imagem.processada" alt="Imagem processada" />
        </template>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const arquivos = ref([])
const imagens = ref([])
const operacao = ref('salt-pepper')
const nivelRuido = ref(0.05)
const escala = ref(50)
const angulo = ref(45)
const carregando = ref(false)
const erro = ref('')

function selecionarArquivos(event) {
  arquivos.value = Array.from(event.target.files)

  imagens.value = arquivos.value.map((arquivo) => ({
    nome: arquivo.name,
    original: URL.createObjectURL(arquivo),
    processada: null
  }))
}

async function processar() {
  carregando.value = true
  erro.value = ''

  const formData = new FormData()

  arquivos.value.forEach((arquivo) => {
    formData.append('arquivos', arquivo)
  })

  formData.append('operacao', operacao.value)
  formData.append('nivelRuido', nivelRuido.value)
  formData.append('escala', escala.value)
  formData.append('angulo', angulo.value)

  try {
    const resposta = await axios.post('http://localhost:8080/processar', formData)

    resposta.data.forEach((item, index) => {
      imagens.value[index].processada = `data:image/png;base64,${item.imagem}`
    })
  } catch (e) {
    erro.value = 'Erro ao processar. Verifique se o backend está rodando.'
  }

  carregando.value = false
}
</script>

<style scoped>
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}

.formulario {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
  margin: 0 auto 30px;
}

input,
select,
button {
  padding: 10px;
}

.galeria {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 10px;
}

img {
  max-width: 100%;
  margin-bottom: 10px;
}

.erro {
  color: red;
}
</style>
