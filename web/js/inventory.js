(() => {
    const CTX = window.CTX || "";

    function tick() {
        const d = new Date().toLocaleString("en-PH", { hour12: false });
        const el = document.getElementById("clock");
        if (el) el.textContent = d;
    }
    tick(); setInterval(tick, 1000);

    const peso = c => "₱" + Number(c).toLocaleString("en-PH", {
        minimumFractionDigits: 2, maximumFractionDigits: 2
    });
    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, c =>
            ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
    }
    function toast(msg, type = "") {
        const el = document.getElementById("toast");
        el.textContent = msg;
        el.className = "toast show " + type;
        setTimeout(() => { el.className = "toast " + type; }, 2200);
    }

    const algoSelect = document.getElementById("algoSelect");
    
    // Default to 'dp' if no preference is saved
    const savedAlgo = localStorage.getItem("posAlgo") || "dp";
    algoSelect.value = savedAlgo;

    algoSelect.addEventListener("change", (e) => {
        const val = e.target.value;
        localStorage.setItem("posAlgo", val);
        toast(`Engine set to ${val === 'dp' ? 'Optimal (DP)' : 'Greedy'}`, "success");
    });

    async function loadMetrics() {
        const r = await fetch(`${CTX}/api/metrics`);
        const m = await r.json();
        document.getElementById("mDatasetSize").textContent = m.datasetSize.toLocaleString();
        document.getElementById("mBinaryTime").textContent = (m.binaryTimeNs / 1000).toFixed(1) + " μs";
        document.getElementById("mBinaryComp").textContent = `${m.binaryComparisons} steps`;
        document.getElementById("mLinearTime").textContent = (m.linearTimeNs / 1000).toFixed(1) + " μs";
        
        const speedup = m.binaryTimeNs > 0 ? (m.linearTimeNs / m.binaryTimeNs).toFixed(1) + "× faster" : "—";
        document.getElementById("mSpeedup").textContent = speedup;

        document.getElementById("mDpUnits").textContent = m.dpUnits >= 0 ? `${m.dpUnits} units` : "n/a";
        const greedy = m.greedyUnits;
        document.getElementById("mGreedyCmp").textContent = greedy >= 0 ? `Greedy: ${greedy} units` : "Greedy: fails";
    }

    document.getElementById("refreshMetricsBtn").addEventListener("click", () => {
        loadMetrics();
        toast("Algorithm Test Complete", "success");
    });

    let page = 1, size = 25, totalPages = 1, query = "";

    async function loadInventory() {
        const r = await fetch(`${CTX}/api/inventory?page=${page}&size=${size}` + (query ? `&q=${encodeURIComponent(query)}` : ""));
        const data = await r.json();
        totalPages = data.totalPages;

        const tbody = document.getElementById("invBody");
        if (data.items.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4"><div class="cart-empty">No items found.</div></td></tr>';
        } else {
            tbody.innerHTML = data.items.map(p => `
                <tr>
                    <td><strong>#${p.id}</strong></td>
                    <td>${escapeHtml(p.name)}</td>
                    <td style="text-align:right;">${peso(p.price)}</td>
                    <td style="text-align:right;">${p.stock.toLocaleString()}</td>
                </tr>
            `).join("");
        }
        document.getElementById("pageInfo").textContent = `Page ${page} of ${totalPages}`;
        document.getElementById("prevBtn").disabled = page <= 1;
        document.getElementById("nextBtn").disabled = page >= totalPages;
    }

    document.getElementById("firstBtn").onclick = () => { page = 1; loadInventory(); };
    document.getElementById("prevBtn").onclick  = () => { if (page > 1) { page--; loadInventory(); } };
    document.getElementById("nextBtn").onclick  = () => { if (page < totalPages) { page++; loadInventory(); } };
    document.getElementById("lastBtn").onclick  = () => { page = totalPages; loadInventory(); };

    async function loadDenoms() {
        const r = await fetch(`${CTX}/api/denominations`);
        const data = await r.json();
        const list = document.getElementById("denomList");
        list.innerHTML = data.denominations.map(d => `
            <div class="denom-toggle-card ${d.available ? "" : "disabled"}" data-denom="${d.denom}">
                <div>
                    <div class="name">${escapeHtml(d.label)}</div>
                    <div class="status">${d.available ? "Available" : "Out of Stock"}</div>
                </div>
                <label class="toggle">
                    <input type="checkbox" ${d.available ? "checked" : ""} data-denom="${d.denom}" />
                    <span class="slider"></span>
                </label>
            </div>
        `).join("");

        list.querySelectorAll('input[type="checkbox"]').forEach(cb => {
            cb.addEventListener("change", async (e) => {
                const denom = e.target.dataset.denom;
                const available = e.target.checked;
                await fetch(`${CTX}/api/denominations`, {
                    method: "POST",
                    headers: { "Content-Type": "application/x-form-urlencoded" },
                    body: `denom=${denom}&available=${available}`
                });
                const card = e.target.closest(".denom-toggle-card");
                card.classList.toggle("disabled", !available);
                card.querySelector(".status").textContent = available ? "Available" : "Out of Stock";
                loadMetrics();
            });
        });
    }

    loadMetrics(); loadInventory(); loadDenoms();
})();