import { useEffect, useState } from 'react'
import { clearRecorded, getReport, listRecorded, listRuns, startReplay } from './api'
import './App.css'

function StatCard({ label, value, tone }) {
  return (
    <div className={`stat ${tone || ''}`}>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

function DiffRow({ diff }) {
  return (
    <div className="diff-row">
      <div className="diff-head">
        <span className={`badge ${diff.type.toLowerCase()}`}>{diff.type}</span>
        <code>{diff.path}</code>
        {diff.breaking && <span className="badge breaking">BREAKING</span>}
      </div>
      <div className="diff-values">
        {diff.expected !== null && <div className="old">- {diff.expected}</div>}
        {diff.actual !== null && <div className="new">+ {diff.actual}</div>}
      </div>
    </div>
  )
}

function CallCard({ call }) {
  return (
    <div className={`call ${call.breaking ? 'call-breaking' : ''}`}>
      <div className="call-title">
        <span className="method">{call.method}</span>
        <code>{call.path}</code>
        <span className="muted">#{call.recordedCallId}</span>
      </div>
      {call.diffs.map((d, i) => (
        <DiffRow key={i} diff={d} />
      ))}
    </div>
  )
}

export default function App() {
  const [runs, setRuns] = useState([])
  const [recordedCount, setRecordedCount] = useState(0)
  const [selected, setSelected] = useState(null)
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function refresh() {
    try {
      const [r, recorded] = await Promise.all([listRuns(), listRecorded()])
      setRuns(r)
      setRecordedCount(recorded.length)
    } catch (e) {
      setError(e.message)
    }
  }

  async function openRun(runId) {
    setSelected(runId)
    setError('')
    try {
      setReport(await getReport(runId))
    } catch (e) {
      setError(e.message)
    }
  }

  async function runReplay() {
    setLoading(true)
    setError('')
    try {
      const run = await startReplay()
      await refresh()
      await openRun(run.runId)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  async function clearAll() {
    if (!window.confirm('Delete all recorded calls? Past replay runs stay.')) {
      return
    }
    setError('')
    try {
      await clearRecorded()
      await refresh()
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    refresh()
  }, [])

  return (
    <div className="layout">
      <aside className="sidebar">
        <h1>API Replay</h1>
        <p className="muted">{recordedCount} recorded calls</p>

        <button className="primary" onClick={runReplay} disabled={loading}>
          {loading ? 'Replaying...' : 'Run replay'}
        </button>
        <button className="danger" onClick={clearAll} disabled={recordedCount === 0}>
          Clear recordings
        </button>

        <h2>Run history</h2>
        {runs.length === 0 && <p className="muted">No runs yet</p>}
        <ul className="runs">
          {runs.map((r) => (
            <li
              key={r.runId}
              className={r.runId === selected ? 'active' : ''}
              onClick={() => openRun(r.runId)}
            >
              <div>{new Date(r.replayedAt).toLocaleString()}</div>
              <div className="muted">
                {r.total} calls · {r.runId.slice(0, 8)}
              </div>
            </li>
          ))}
        </ul>
      </aside>

      <main className="content">
        {error && <div className="error">{error}</div>}

        {!report && !error && (
          <p className="muted">Run a replay or pick one from the history.</p>
        )}

        {report && (
          <>
            <div className="stats">
              <StatCard label="Total" value={report.total} />
              <StatCard label="Unchanged" value={report.unchanged} tone="good" />
              <StatCard label="Changed" value={report.changed} tone="warn" />
              <StatCard label="Breaking" value={report.breaking} tone="bad" />
            </div>

            {report.calls.length === 0 ? (
              <p className="all-good">No differences. Everything matches.</p>
            ) : (
              report.calls.map((c) => <CallCard key={c.recordedCallId} call={c} />)
            )}
          </>
        )}
      </main>
    </div>
  )
}