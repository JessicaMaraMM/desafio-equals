import { useMemo, useState } from "react";

export default function App() {
  const API = "/api";

  const [file, setFile] = useState(null);

  const [result, setResult] = useState(null);
  const [sales, setSales] = useState([]);

  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [loadingImport, setLoadingImport] = useState(false);
  const [loadingSales, setLoadingSales] = useState(false);
  const [loadingFilter, setLoadingFilter] = useState(false);

  function clearMessages({ clearResult = false } = {}) {
    setError("");
    setSuccess("");
    if (clearResult) setResult(null);
  }

  async function parseResponse(response) {
    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");

    const payload = isJson ? await response.json() : await response.text();

    if (!response.ok) {
      const message =
        (typeof payload === "object" && payload?.error) ||
        (typeof payload === "string" && payload.trim()) ||
        `Falha na requisição (${response.status})`;

      throw new Error(message);
    }

    return payload;
  }

  async function fetchSales(params = "") {
    setLoadingSales(true);
    try {
      const response = await fetch(`${API}/sales${params}`);
      const data = await parseResponse(response);
      const list = Array.isArray(data) ? data : [];
      setSales(list);
      return list;
    } finally {
      setLoadingSales(false);
    }
  }

  const importSummary = useMemo(() => {
    if (!result) return null;

    const total = result.totalLines ?? 0;
    const detail = result.detailLines ?? 0;
    const saved = result.saved ?? 0;
    const ignored = result.ignored ?? 0;
    const invalid = result.invalid ?? 0;
    const errors = Array.isArray(result.errors) ? result.errors : [];

    return { total, detail, saved, ignored, invalid, errors };
  }, [result]);

  async function handleImport() {
    clearMessages({ clearResult: true });

    if (!file) {
      setSuccess("");
      setError("Selecione um arquivo .txt para importar.");
      return;
    }

    const name = (file.name || "").toLowerCase();
    if (!name.endsWith(".txt")) {
      setSuccess("");
      setError("O arquivo precisa ser .txt.");
      return;
    }

    setLoadingImport(true);

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await fetch(`${API}/imports`, {
        method: "POST",
        body: formData,
      });

      const data = await parseResponse(response);
      setResult(data);

      const saved = data?.saved ?? 0;
      const invalid = data?.invalid ?? 0;

      setError("");
      setSuccess(`Importação concluída. Salvas: ${saved}. Inválidas: ${invalid}.`);

      await fetchSales("");
    } catch (err) {
      setSuccess("");
      setError(err?.message || "Erro ao importar arquivo.");
    } finally {
      setLoadingImport(false);
    }
  }

  async function handleFilter() {
    clearMessages();
    if (loadingFilter) return;

    setLoadingFilter(true);

    try {
      if (!start && !end) {
        const list = await fetchSales("");
        setError("");
        setSuccess(`Exibindo todas as vendas (${list.length}).`);
        return;
      }

      if (start && end && start > end) {
        setSuccess("");
        setError("A data inicial não pode ser maior que a data final.");
        return;
      }

      const params = new URLSearchParams();
      if (start) params.append("start", start);
      if (end) params.append("end", end);

      const query = `?${params.toString()}`;
      const list = await fetchSales(query);

      if (list.length === 0) {
        setSuccess("");
        if (start && !end) setError("Nenhuma venda encontrada a partir da data inicial informada.");
        else if (!start && end) setError("Nenhuma venda encontrada até a data final informada.");
        else setError("Nenhuma venda encontrada no período informado.");
      } else {
        setError("");
        setSuccess(`Filtro aplicado. Encontradas: ${list.length}.`);
      }
    } catch (err) {
      setSuccess("");
      setError(err?.message || "Erro ao filtrar vendas.");
    } finally {
      setLoadingFilter(false);
    }
  }

  async function handleClearFilter() {
    if (loadingFilter) return;

    clearMessages();
    setStart("");
    setEnd("");

    setLoadingFilter(true);
    try {
      const list = await fetchSales("");
      setError("");
      setSuccess(`Filtro limpo. Exibindo todas as vendas (${list.length}).`);
    } catch (err) {
      setSuccess("");
      setError(err?.message || "Erro ao limpar filtro.");
    } finally {
      setLoadingFilter(false);
    }
  }

  function moneyBR(value) {
    const n = Number(value);
    if (Number.isNaN(n)) return "0,00";
    return n.toLocaleString("pt-BR", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    });
  }

  const busy = loadingImport || loadingSales || loadingFilter;

  return (
    <div style={{ padding: 20 }}>
      <h1>Relatório de Vendas</h1>

      {(error || success) && (
        <div
          className={`messages-container${error ? ' error' : ' success'}`}
        >
          {error && (
            <span style={{ fontWeight: 600 }} role="alert" aria-live="assertive">
              {error}
            </span>
          )}
          {success && (
            <span style={{ fontWeight: 600 }} role="status" aria-live="polite">
              {success}
            </span>
          )}
          <button
            onClick={() => clearMessages({ clearResult: false })}
            disabled={busy}
            title="Fechar"
            aria-label="Fechar mensagem"
            className="clear-messages-btn"
          >
            ×
          </button>
        </div>
      )}

      {/* IMPORTAR */}
      <h2>Importar Arquivo</h2>
      <input
        type="file"
        accept=".txt,text/plain"
        onChange={(e) => setFile(e.target.files?.[0] || null)}
        disabled={busy}
      />{" "}
      <button onClick={handleImport} disabled={busy || !file}>
        {loadingImport ? "Importando..." : "Importar"}
      </button>

      {
        importSummary && (
          <div style={{ marginTop: 12 }}>
            <p>
              <strong>Resumo:</strong> Total: {importSummary.total} | Detalhes:{" "}
              {importSummary.detail} | Salvas: {importSummary.saved} | Ignoradas:{" "}
              {importSummary.ignored} | Inválidas: {importSummary.invalid}
            </p>

            {importSummary.errors.length > 0 && (
              <>
                <p>
                  <strong>Erros (até o limite retornado):</strong>
                </p>
                <ul>
                  {importSummary.errors.map((e, idx) => (
                    <li key={idx}>
                      Linha {e.line}: {e.reason}
                    </li>
                  ))}
                </ul>
              </>
            )}
          </div>
        )
      }

      <hr />

      {/* FILTRO */}
      <h2>Filtro por Data</h2>
      <label>
        Início:{" "}
        <input
          type="date"
          value={start}
          onChange={(e) => setStart(e.target.value)}
          disabled={busy}
        />
      </label>{" "}
      <label>
        Fim:{" "}
        <input
          type="date"
          value={end}
          onChange={(e) => setEnd(e.target.value)}
          disabled={busy}
        />
      </label>{" "}
      <button onClick={handleFilter} disabled={busy}>
        {loadingFilter ? "Filtrando..." : "Filtrar"}
      </button>{" "}
      <button
        onClick={handleClearFilter}
        disabled={busy || (!start && !end)}
        title="Remove datas e volta a listar tudo"
      >
        {loadingFilter ? "Limpando..." : "Limpar filtro"}
      </button>

      <hr />

      {/* VENDAS */}
      <h2>Vendas</h2>
      {loadingSales ? <p>Carregando...</p> : <p>Total: {sales.length}</p>}

      {
        sales.length === 0 ? (
          <p>Nenhuma venda encontrada.</p>
        ) : (
          <table border="1" cellPadding="5">
            <thead>
              <tr>
                <th>ID</th>
                <th>Estabelecimento</th>
                <th>Data</th>
                <th>Hora</th>
                <th>Bandeira</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {sales.map((sale) => (
                <tr key={sale.id}>
                  <td>{sale.id}</td>
                  <td>{sale.establishmentCode}</td>
                  <td>{sale.eventDate}</td>
                  <td>{sale.eventTime || "—"}</td>
                  <td>{sale.brand}</td>
                  <td>{moneyBR(sale.totalAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      }
    </div >
  );
}