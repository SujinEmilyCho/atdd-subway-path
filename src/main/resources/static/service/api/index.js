const METHOD = {
  PUT() {
    return {
      method: 'PUT'
    }
  },
  DELETE() {
    return {
      method: 'DELETE'
    }
  },
  POST(data) {
    return {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ...data
      })
    }
  }
}

const api = (() => {
  const request = (uri, config) => fetch(uri, config).then(data => data.json())

  const line = {
    getAll() {
      return request(`/map`)
    }
  }

  const path = {
    find(params) {
      return request(`/path?source=${params.source}&target=${params.target}&type=${params.type}`)
    }
  }

  return {
    line,
    path
  }
})()

export default api
