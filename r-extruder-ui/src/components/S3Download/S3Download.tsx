import React, { useState } from "react";
import axios from "axios";
import "./S3Download.scss";

const S3Download: React.FC = () => {
  const [fileName, setFileName] = useState("");

  const handleDownload = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/extractors/s3/${fileName}`,
        { responseType: "arraybuffer" }
      );
      const blob = new Blob([response.data], {
        type: response.headers["content-type"],
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error("Error downloading file:", error);
    }
  };

  return (
    <div className="s3-download">
      <h2>Download your Word document</h2>
      <input
        type="text"
        value={fileName}
        onChange={(e) => setFileName(e.target.value)}
        placeholder="Enter S3 key"
      />
      <button onClick={handleDownload}>Download</button>
    </div>
  );
};

export default S3Download;
