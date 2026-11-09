async function request(url, options) {
  const res = await fetch(url, options)
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json()
}

export const listRuns = () => request('/api/replay')
export const startReplay = () => request('/api/replay', { method: 'POST' })
export const getReport = (runId) => request(`/api/replay/${runId}/report`)
export const listRecorded = () => request('/api/recorded')

export async function clearRecorded() {
  const res = await fetch('/api/recorded', { method: 'DELETE' })
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
}