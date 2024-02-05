import React from "react";
import "./Rextruder.scss";
import WordDownload from "../../components/WordDownload";
import RUpload from "../../components/RUpload";

const Rextruder = () => {
  return (
    <main className={"r-extruder"}>
      <basf-header title={"R-extruder"}></basf-header>
      <div className="content">
        <div className="r-upload">
          <RUpload />
        </div>
        <div className="word-download">
          <WordDownload />
        </div>
      </div>
      <basf-footer footer-title={"R-extruder"}></basf-footer>
    </main>
  );
};

export default Rextruder;
