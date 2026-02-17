import { useEffect, useState } from "react";

export default function App() {
  const API = "http://localhost:8080";

  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [sales, setSales] = useState([]);
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");

  useEffect(() => {
    fetchSales();
  }, []);

  async function fetchSales(params = "") {
    const response = await fetch(`${API}/sales${params}`);
    const data = await response.json();
    setSales(data);
  }

  async function handleImport() {
    if (!file) {
      alert("Selecione um arquivo");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API}/imports`, {
      method: "POST",
      body: formData,
    });

    const data = await response.json();
    setResult(data);

    fetchSales();
  }

  async function handleFilter() {
    const params = new URLSearchParams();
    if (start) params.append("start", start);
    if (end) params.append("end", end);

    const query = params.toString() ? `?${params.toString()}` : "";
    fetchSales(query);
  }

  return (
    <div style={{ padding: 20 }}>
      <h1>Relatório de Vendas</h1>

      <h2>Importar Arquivo</h2>
      <input type="file" onChange={(e) => setFile(e.target.files[0])} />
      <button onClick={handleImport}>Importar</button>

      {result && (
        <pre>{JSON.stringify(result, null, 2)}</pre>
      )}

      <hr />

      <h2>Filtro por Data</h2>
      <input type="date" value={start} onChange={(e) => setStart(e.target.value)} />
      <input type="date" value={end} onChange={(e) => setEnd(e.target.value)} />
      <button onClick={handleFilter}>Filtrar</button>

      <hr />

      <h2>Vendas</h2>
      <p>Total: {sales.length}</p>

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
              <td>{sale.eventTime}</td>
              <td>{sale.brand}</td>
              <td>
                {Number(sale.totalAmount).toLocaleString("pt-BR", {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2
                })}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}