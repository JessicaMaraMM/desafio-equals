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
  const [loadingFilter, _setLoadingFilter] = useState(false);

  function clearMessages({ clearResult = false } = {}) {
    setError("");
    setSuccess("");
    if (clearResult) setResult(null);
  }

  async function parseResponse(response) {
    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");

    let payload;
    if (isJson) payload = await response.json();
    else payload = await response.text();

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
      setSales(Array.isArray(data) ? data : []);
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
      setError("Selecione um arquivo .txt para importar.");
      return;
    }

    const name = (file.name || "").toLowerCase();
    if (!name.endsWith(".txt")) {
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

      setSuccess(`Importação concluída. Salvas: ${saved}. Inválidas: ${invalid}.`);

      await fetchSales();
    } catch (err) {
      setError(err?.message || "Erro ao importar arquivo.");
    } finally {
      setLoadingImport(false);
    }
  }

  async function handleFilter() {
    setError("");

    if (!start && !end) {
      await fetchSales("");
      return;
    }

    if (start && end && start > end) {
      setError("A data inicial não pode ser maior que a data final.");
      return;
    }

    const params = new URLSearchParams();
    if (start) params.append("start", start);
    if (end) params.append("end", end);

    const query = `?${params.toString()}`;

    try {
      await fetchSales(query);
    } catch (err) {
      setError(err?.message || "Erro ao filtrar vendas.");
    }
  }
  
  async function handleClearFilter() {
    clearMessages();
    setStart("");
    setEnd("");
    await fetchSales();
    setSuccess("Filtro limpo. Exibindo todas as vendas.");
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

      {/* Mensagens */}
      {(error || success) && (
        <div style={{ marginBottom: 10 }}>
          {error && (
            <p style={{ color: "red" }} role="alert" aria-live="assertive">
              {error}
            </p>
          )}
          {success && (
            <p style={{ color: "green" }} role="status" aria-live="polite">
              {success}
            </p>
          )}

          <button
            onClick={() => clearMessages({ clearResult: false })}
            disabled={busy}
            title="Limpa mensagens de erro/sucesso"
          >
            Limpar mensagens
          </button>
        </div>
      )}

      {/* Import */}
      <h2>Importar Arquivo</h2>
      <input
        type="file"
        accept=".txt,text/plain"
        onChange={(e) => setFile(e.target.files?.[0] || null)}
        disabled={busy}
      />
      <button onClick={handleImport} disabled={busy || !file}>
        {loadingImport ? "Importando..." : "Importar"}
      </button>

      {importSummary && (
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
      )}

      <hr />

      {/* Filtro */}
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
        Limpar filtro
      </button>

      <hr />

      {/* Vendas */}
      <h2>Vendas</h2>
      <button
        onClick={() =>
          fetchSales().catch((err) =>
            setError(err?.message || "Erro ao carregar vendas.")
          )
        }
        disabled={busy}
      >
        {loadingSales ? "Carregando..." : "Atualizar"}
      </button>

      <p>Total: {sales.length}</p>

      {sales.length === 0 ? (
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
      )}
    </div>
  );
}